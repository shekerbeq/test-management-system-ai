package kz.testmanagement.aigenerator.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import kz.testmanagement.aigenerator.parser.QuestionParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WireMockLlmContractTest {

    private final WireMockServer server = new WireMockServer(0);

    @AfterEach
    void stopServer() {
        if (server.isRunning()) {
            server.stop();
        }
    }

    @Test
    void openAiCompatibleClientParsesChatCompletionResponse() {
        server.start();
        WireMock.configureFor("localhost", server.port());
        server.stubFor(post(urlEqualTo("/v1/chat/completions"))
                .willReturn(okJson("""
                        {"choices":[{"message":{"content":"[{\\"question\\":\\"Q\\",\\"options\\":[\\"A\\",\\"B\\"],\\"correct\\":0,\\"correctIndices\\":[0],\\"open\\":false}]"}}]}
                        """)));

        OpenAIClientImpl client = new OpenAIClientImpl(
                WebClient.builder(),
                "test-key",
                "gpt-4o-mini",
                server.baseUrl() + "/v1/chat/completions",
                new QuestionParser()
        );

        var questions = client.generateQuestions("prompt");

        assertEquals(1, questions.size());
        assertEquals("Q", questions.get(0).getQuestion());
    }
}
