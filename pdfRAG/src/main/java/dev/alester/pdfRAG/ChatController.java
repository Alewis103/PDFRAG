package dev.alester.pdfRAG;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("api/chat")
public class ChatController {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/Theology-of-arithmetic.st")
    private Resource ragPromptTemplate;

    public ChatController(ChatClient chatClient, VectorStore vectorStore){
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    @GetMapping(value= "/ask", produces= "text/plain")
    public String ask(@RequestParam(value = "message", defaultValue = "Can you explain the Foreword of the document like I am 5") String message){
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.query(message).withTopK(2));
        List<String> contentList = similarDocuments.stream().map(Document::getContent).toList();
        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("input", message);
        promptParameters.put("documents", String.join("\n", contentList));
        Prompt prompt = promptTemplate.create(promptParameters);

        return chatClient.call(prompt).getResult().getOutput().getContent();
    }

}
