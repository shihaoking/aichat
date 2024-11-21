// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package org.simon.aichat.claude3;

// snippet-start:[bedrock-runtime.java2.ConverseAsync_AnthropicClaude]
// Use the Converse API to send a text message to Anthropic Claude
// with the async Java client.

import org.apache.commons.lang3.StringUtils;
import org.simon.aichat.websocket.ChatStreamMessage;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseStreamResponseHandler;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ConverseAsync {
    
    public Message converseAsync(List<Message> messages) {

        // Create a Bedrock Runtime client in the AWS Region you want to use.
        // Replace the DefaultCredentialsProvider with your preferred credentials provider.
        var client = BedrockRuntimeAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.US_WEST_2)
                .build();

        // Set the model ID, e.g., Claude 3 Sonnet.
        var modelId = "anthropic.claude-3-opus-20240229-v1:0";

        // Send the message with a basic inference configuration.
        var request = client.converse(params -> params
                .modelId(modelId)
                .messages(messages)
                .inferenceConfig(config -> config
                        .maxTokens(2048)
                        .temperature(0.5F)
                        .topP(0.9F))
        );

        // Prepare a future object to handle the asynchronous response.
        CompletableFuture<Message> future = new CompletableFuture<>();

        // Handle the response or error using the future object.
        request.whenComplete((response, error) -> {
            if (error == null) {
                System.out.println("Claude process latency(ms): " + response.metrics().latencyMs());
                // Extract the generated text from Bedrock's response object.
                Message responseMessage = response.output().message();
                future.complete(responseMessage);
            } else {
                future.completeExceptionally(error);
            }
        });

        try {
            // Wait for the future object to complete and retrieve the generated text.
            Message responseMessage = future.get();

            client.close();

            return responseMessage;
        } catch (ExecutionException | InterruptedException e) {
            System.err.printf("Can't invoke '%s': %s", modelId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void converseStream(List<Message> messages, Consumer<String> contentBlockDeltaComsumer, Consumer<String> onMessageStartComnsumer, Consumer<ChatStreamMessage> onMessageStopComnsumer) {
        StringBuilder realTimeResponse = new StringBuilder();

        // Create a Bedrock Runtime client in the AWS Region you want to use.
        // Replace the DefaultCredentialsProvider with your preferred credentials provider.
        var client = BedrockRuntimeAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.US_WEST_2)
                .build();

        // Set the model ID, e.g., Claude 3 Haiku.
        var modelId = "anthropic.claude-3-5-haiku-20241022-v1:0";

        String regex = "(.*?)([,，.。!！:：])(.*)";
        Pattern pattern = Pattern.compile(regex);
        StringBuilder fullContant = new StringBuilder();

        // Create a handler to extract and print the response text in real-time.
        var responseStreamHandler = ConverseStreamResponseHandler.builder()
                .subscriber(ConverseStreamResponseHandler.Visitor.builder()
                        .onContentBlockDelta(chunk -> {
                            String responseText = chunk.delta().text();
                            System.out.println("RealTimeResponseMessage: " + responseText);
                            fullContant.append(responseText);
                            Matcher matcher = pattern.matcher(responseText);

                            // 判断本次响应内容是否包含标点符号
                            if (matcher.matches()) {
                                realTimeResponse.append(matcher.group(1));//标点符号前面的内容
                                realTimeResponse.append(matcher.group(2));//标点符号

                                //带输出的内容长度大于10后才输出，小于10的话就继续拼接
                                if (realTimeResponse.length() > 10) {
                                    String respStr = realTimeResponse.toString();
                                    respStr = respStr.replaceAll("[\s\n\r]]", "");
                                    if (respStr.length() > 0) {
                                        //将当前已经断好句的内容输出
                                        contentBlockDeltaComsumer.accept(respStr);
                                    }
                                    //重置实时输出内容，并将标点符号后面的内容追加进去
                                    realTimeResponse.setLength(0);
                                    realTimeResponse.append(matcher.group(3));
                                } else {
                                    realTimeResponse.append(matcher.group(3));
                                }
                            } else {//本段响应内容没有标点符号则直接追加到待输出的文本中
                                realTimeResponse.append(responseText);
                            }

                        }).onMessageStart(c -> {
                            System.out.println("Claude start response" + Calendar.getInstance().getTime());

                            if (onMessageStartComnsumer != null) {
                                onMessageStartComnsumer.accept(c.roleAsString());
                            }
                        }).onMessageStop(c -> {
                            System.out.println("Claude finished response" + Calendar.getInstance().getTime());
                            System.out.println("FullResponseMessage: " + fullContant);

                            //最后剩余部分输出
                            if (StringUtils.isNoneBlank(realTimeResponse.toString())) {
                                ChatStreamMessage chatStreamMessage = new ChatStreamMessage();
                                chatStreamMessage.setStreamMessage(realTimeResponse.toString());
                                chatStreamMessage.setFinallMessage(fullContant.toString());
                                onMessageStopComnsumer.accept(chatStreamMessage);
                            }
                        }).build()
                ).onError(err ->
                        System.err.printf("Can't invoke '%s': %s", modelId, err.getMessage())
                ).build();

        try {
            // Send the message with a basic inference configuration and attach the handler.
            System.out.println("Start as calude" + Calendar.getInstance().getTime());
            client.converseStream(request -> request.modelId(modelId)
                    .messages(messages)
                    .inferenceConfig(config -> config
                            .maxTokens(256)
                            .temperature(0.5F)
                            .topP(0.9F)
                    ), responseStreamHandler).get();

        } catch (ExecutionException | InterruptedException e) {
            System.err.printf("Can't invoke '%s': %s", modelId, e.getCause().getMessage());
        }

    }
}
// snippet-end:[bedrock-runtime.java2.ConverseAsync_AnthropicClaude]
