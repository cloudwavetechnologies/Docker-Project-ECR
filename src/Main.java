package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Main implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event event, Context context) {
        context.getLogger().log("🚀 Lambda triggered by S3 event!\n");

        event.getRecords().forEach(record -> {
            String bucket = record.getS3().getBucket().getName();
            String key = record.getS3().getObject().getKey();
            context.getLogger().log("📦 File uploaded: " + bucket + "/" + key + "\n");
        });

        return "✅ Lambda executed successfully!";
    }
}
