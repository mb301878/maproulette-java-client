package org.maproulette.client.utilities;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.maproulette.client.model.PriorityRule;
import org.maproulette.client.model.RuleList;

import java.io.IOException;
import java.util.ArrayList;

public class ObjectMapperFactory {
    private static volatile ObjectMapper mapper;

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            synchronized (ObjectMapper.class) {
                if (mapper == null) {
                    mapper = new ObjectMapper();
                    SimpleModule module = new SimpleModule();
                    module.addSerializer(RuleList.class, new RuleListSerializer());
                    module.addDeserializer(RuleList.class, new RuleListDeserializer());
                    mapper.registerModule(module);
                }
            }
        }

        return mapper;
    }

    private static class RuleListSerializer extends JsonSerializer<RuleList> {

        @Override
        public void serialize(RuleList value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        }
    }


    private static class RuleListDeserializer extends JsonDeserializer<RuleList> {

        @Override
        public RuleList deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            return buildRuleListHelper(node);
        }

        private static RuleList buildRuleListHelper(JsonNode node) {
            final RuleList ret = RuleList.builder()
                    .condition(node.get("condition").asText())
                    .ruleList(new ArrayList<>())
                    .rules(new ArrayList<>())
                    .build();

            for (JsonNode it : node.withArray("rules")) {
                if (it.get("condition") != null) {
                    // If the child is a PriorityRule, do a recursive call to build the rule and add it to the list.
                    RuleList child = buildRuleListHelper(it);
                    ret.getRuleList().add(child);
                } else {
                    PriorityRule priorityRule = PriorityRule.builder()
                            .type(it.get("type").asText())
                            .operator(it.get("operator").asText())
                            .value(it.get("value").asText())
                            .build();
                    ret.getRules().add(priorityRule);
                }
            }
            if (ret.getRuleList().isEmpty()) {
                ret.setRuleList(null);
            }
            if (ret.getRules().isEmpty()) {
                ret.setRules(null);
            }

            return ret;
        }
    }
}
