package org.sprain.ai.config.model;

import org.springframework.ai.chat.model.ChatModel;

public class ModelHelper {
    public static String getModelName(ChatModel model) {
        return model.getDefaultOptions().getModel();
    }
}
