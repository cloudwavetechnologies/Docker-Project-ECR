package com.cloudwavetechnologies;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main implements RequestHandler<S3Event, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(S3Event event, Context context) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            context.getLogger().log("\u001B[36m🚀 Lambda triggered by S3 event!\u001B[0m\n");

            event.getRecords().forEach(record -> {
                String bucket = record.getS3().getBucket().getName();
                String key = record.getS3().getObject().getKey();
                context.getLogger().log("\u001B[32m📦 File uploaded: " + bucket + "/" + key + "\u001B[0m\n");

                response.put("bucket", bucket);
                response.put("file", key);
            });

            response.put("status", "✅ Success");
            response.put("timestamp", Instant.now().toString());
            response.put("color", "#4CAF50");
            response.put("poweredBy", "🌊 CloudWave Technologies");
            response.put("inspiration", "You’ll see examples that could inspire:\n" +
                "- 🌐 Website headers or landing pages\n" +
                "- 🧾 Branded reports or dashboards\n" +
                "- 🏭 Product packaging or service brochures\n" +
                "Let us help you design a custom output image with your logo, color palette, and tagline—something that truly represents your brand.");

        } catch (Exception e) {
            context.getLogger().log("\u001B[31m❌ ERROR: " + e.getMessage() + "\u001B[0m\n");

            response.put("status", "❌ Error");
            response.put("errorType", e.getClass().getSimpleName());
            response.put("errorMessage", e.getMessage());
            response.put("timestamp", Instant.now().toString());
            response.put("color", "#FF4C4C");
            response.put("poweredBy", "🌊 CloudWave Technologies");
        }

        return response;
    }
}
