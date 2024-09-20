// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package org.simon.aichat.claude3;

// snippet-start:[bedrock-runtime.java2.ConverseAsync_AnthropicClaude]
// Use the Converse API to send a text message to Anthropic Claude
// with the async Java client.

import org.simon.aichat.common.AwsCredentialsGenerate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ConverseAsync {
    
    public Message converseAsync(List<Message> inputs) {

        // Create a Bedrock Runtime client in the AWS Region you want to use.
        // Replace the DefaultCredentialsProvider with your preferred credentials provider.
        var client = BedrockRuntimeAsyncClient.builder()
                //.credentialsProvider(DefaultCredentialsProvider.create())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsCredentialsGenerate.generateBasicCredential()))
                .region(Region.US_EAST_1)
                .build();

        // Set the model ID, e.g., Claude 3 Sonnet.
        var modelId = "anthropic.claude-3-5-sonnet-20240620-v1:0";

        // Send the message with a basic inference configuration.
        var request = client.converse(params -> params
                .modelId(modelId)
                .messages(inputs)
                .inferenceConfig(config -> config
                        .maxTokens(1024)
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
}
// snippet-end:[bedrock-runtime.java2.ConverseAsync_AnthropicClaude]
