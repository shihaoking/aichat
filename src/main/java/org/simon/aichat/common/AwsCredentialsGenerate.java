package org.simon.aichat.common;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;


public class AwsCredentialsGenerate {
    public static AwsBasicCredentials generateBasicCredential() {
        // 硬编码的凭证
        String accessKeyId = "AKIAQMPKLMXTD5HAOTGC"; // 替换为你的Access Key
        String secretAccessKey = "89W1umWfweo+TJrYWpBnpF5CCcozP4A+gcbihu9o"; // 替换为你的Secret Key

        // 创建AWS凭证
        return AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    }
}
