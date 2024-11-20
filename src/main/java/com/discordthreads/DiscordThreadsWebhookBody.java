package com.discordthreads;

import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;

@Data
public class DiscordThreadsWebhookBody {
    private String content;
    private String thread_name;
    private long[] applied_tags;
    private Embed embed;

    public DiscordThreadsWebhookBody() {
        this.applied_tags = new long[0];
    }

    public void setContent(String levelUpString) {
        content = levelUpString;
    }

    public void setThreadName(String threadname) {
        thread_name = threadname;
    }

    public void addAppliedTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Tag cannot be null or empty");
        }

        try {
            // Convert the tag to a long
            long tagId = Long.parseLong(tag);

            // Add the tag to the array
            ArrayList<Long> tagList = new ArrayList<>();
            for (long t : applied_tags) {
                tagList.add(t);
            }
            tagList.add(tagId);

            // Update the applied_tags array
            applied_tags = tagList.stream().mapToLong(Long::longValue).toArray();

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tag must be a valid numerical ID");
        }
    }
    @Data
    static class Embed
    {
        final UrlEmbed image;

        Embed(UrlEmbed image) {
            this.image = image;
        }
    }

    @Data
    static class UrlEmbed
    {
        final String url;

        UrlEmbed(String url) {
            this.url = url;
        }
    }


}
