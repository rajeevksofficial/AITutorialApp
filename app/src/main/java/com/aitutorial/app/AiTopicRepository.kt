package com.aitutorial.app

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object AiTopicRepository {

    private val foundationTopics = listOf(
        AiTopic(
            id = "rag",
            title = "Retrieval-Augmented Generation (RAG)",
            wikiSlug = "Retrieval-augmented_generation",
            category = "Generative AI",
            difficulty = "Intermediate",
            readTime = "6 min",
            shortSummary = "An architecture that enriches LLM prompts with dynamic external knowledge retrieved from databases or documents to eliminate hallucinations and ground outputs.",
            coreConcepts = listOf(
                "Vector Embeddings: Converting unstructured text into high-dimensional numerical vectors.",
                "Dense Retrieval: Searching documents via Cosine Similarity / Dot Product.",
                "Chunking Strategies: Splitting long documents into semantically coherent fragments.",
                "Prompt Synthesis: Injecting retrieved context alongside user query into the context window.",
                "Hallucination Mitigation: Providing verifiable citations and reference anchors."
            ),
            tutorialContent = """
                Retrieval-Augmented Generation (RAG) is one of the most critical patterns in modern AI application engineering. Instead of relying solely on an LLM's static training data, RAG pairs a retrieval mechanism with a generator model.

                1. The Indexing Phase:
                Documents (PDFs, docs, wiki pages) are chunked, transformed into embedding vectors using models like text-embedding-3 or open-source embeddings, and stored in vector databases (e.g. Pinecone, Chroma, Milvus, pgvector).

                2. The Retrieval Phase:
                When a user asks a question, the query is converted into an embedding. The vector database performs an Approximate Nearest Neighbor (ANN) search to find the most relevant chunks.

                3. The Generation Phase:
                The retrieved chunks are formatted into a system prompt: 'Based on the following context: [context], answer the question: [question]'. The LLM synthesizes an accurate, factually grounded response with exact citations.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Retrieval-augmented_generation"
        ),

        AiTopic(
            id = "transformers",
            title = "Transformer Architecture & Self-Attention",
            wikiSlug = "Transformer_(deep_learning_architecture)",
            category = "Deep Learning",
            difficulty = "Advanced",
            readTime = "8 min",
            shortSummary = "The foundational neural network architecture introduced in 'Attention Is All You Need' that powers modern LLMs through parallelized self-attention mechanisms.",
            coreConcepts = listOf(
                "Self-Attention Mechanism: Calculating dynamic token-to-token importance weights.",
                "Multi-Head Attention: Allowing the model to attend to information at different representation subspaces.",
                "Positional Encodings: Injecting sequence order since Transformers process tokens concurrently.",
                "Residual Connections & LayerNorm: Stabilizing deep neural gradient flows."
            ),
            tutorialContent = """
                Before the Transformer (2017), Natural Language Processing relied on sequential recurrent architectures (RNNs and LSTMs) which suffered from vanishing gradients and couldn't process sentences in parallel.

                The Transformer revolution replaced recurrence entirely with Self-Attention.
                Key Equation:
                Attention(Q, K, V) = softmax((Q * K^T) / sqrt(d_k)) * V

                - Query (Q): What the token is looking for.
                - Key (K): What information the token holds.
                - Value (V): The actual content represented.

                By parallelizing token computations across modern GPUs, Transformers enabled the scaling of models from millions of parameters to hundreds of billions.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Transformer_(deep_learning_architecture)"
        ),

        AiTopic(
            id = "llm",
            title = "Large Language Models (LLMs)",
            wikiSlug = "Large_language_model",
            category = "Generative AI",
            difficulty = "Beginner",
            readTime = "5 min",
            shortSummary = "Massive neural networks trained on vast web corpora to understand, summarize, generate, and reason across natural human language and code.",
            coreConcepts = listOf(
                "Pre-training: Self-supervised next-token prediction over trillions of text tokens.",
                "Scaling Laws: Empirical observations that loss drops predictably with increased compute, data, and parameters.",
                "In-Context Learning: The emergent capability to learn tasks purely from prompt examples without weights update.",
                "Instruction Fine-Tuning: Adapting raw token completion models into helpful conversational assistants."
            ),
            tutorialContent = """
                Large Language Models (LLMs) represent the frontier of generative AI. Models like GPT-4, Gemini, Claude, and Llama are trained by predicting the next token in sequence across internet-scale datasets.

                Life Cycle of an LLM:
                1. Pre-training: Heavy compute (thousands of GPUs) learning general grammar, logic, world knowledge, and programming.
                2. Supervised Fine-Tuning (SFT): Demonstrations of high quality answers, formatting, and task completion.
                3. Reinforcement Learning (RLHF / DPO): Aligning behavior with human values, safety criteria, and nuanced preferences.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Large_language_model"
        ),

        AiTopic(
            id = "ai_agents",
            title = "Autonomous AI Agents & Tool Calling",
            wikiSlug = "Intelligent_agent",
            category = "AI Agents",
            difficulty = "Intermediate",
            readTime = "7 min",
            shortSummary = "Autonomous systems that leverage LLMs as reasoning engines to break down complex goals, invoke external APIs/tools, and execute iterative action loops.",
            coreConcepts = listOf(
                "ReAct Pattern: Interleaving 'Thought' (reasoning), 'Action' (tool invocation), and 'Observation' (result parsing).",
                "Function Calling: Structured output generation (JSON schemas) to execute database queries or APIs.",
                "Agentic Memory: Short-term scratchpad context and long-term vector database persistence.",
                "Multi-Agent Collaboration: Coordinating specialized agents (Researcher, Coder, Reviewer) to accomplish complex workflows."
            ),
            tutorialContent = """
                While standard chatbots produce a single answer, AI Agents possess agency: the ability to take actions in an environment to achieve an overarching objective.

                Architecture of an AI Agent:
                1. Planning: Breaking down large user requests into atomic executable subtasks.
                2. Memory: Keeping track of previous thoughts, attempted tools, and error recoveries.
                3. Tools: Executing shell commands, web searches, SQL queries, code sandboxes, and file modifications.
                4. Evaluation Loop: Checking whether the action's result meets the target criteria before proceeding or terminating.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Intelligent_agent"
        ),

        AiTopic(
            id = "prompt_engineering",
            title = "Prompt Engineering & In-Context Learning",
            wikiSlug = "Prompt_engineering",
            category = "Generative AI",
            difficulty = "Beginner",
            readTime = "4 min",
            shortSummary = "The discipline of designing optimal prompts and structuring inputs to guide generative AI models toward accurate and reliable outputs.",
            coreConcepts = listOf(
                "Few-Shot Prompting: Providing input-output demonstration pairs inside the prompt.",
                "Chain-of-Thought (CoT): Instructing the model to 'think step by step' before answering.",
                "Role / Persona Prompting: Setting authoritative expertise context to condition model style and depth.",
                "Delimiters & Structured Outputs: Enforcing JSON / XML tags to prevent prompt injection and parse results reliably."
            ),
            tutorialContent = """
                Prompt engineering is both an art and an engineering science. Effective prompt design directly influences the attention allocation inside the neural network.

                Key Best Practices:
                - Give Clear Constraints: Specify what TO do and what NOT to do explicitly.
                - Provide Examples: 2 or 3 curated few-shot examples dramatically boost zero-shot performance.
                - Break Complex Tasks Down: Chain multiple prompts rather than trying to accomplish 10 things in a single query.
                - Guard Against Injection: Treat untrusted user inputs with distinct boundary tags.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Prompt_engineering"
        ),

        AiTopic(
            id = "diffusion_models",
            title = "Diffusion Models & Generative Images",
            wikiSlug = "Diffusion_model",
            category = "Vision & Multimodal",
            difficulty = "Advanced",
            readTime = "7 min",
            shortSummary = "Generative models that learn to create high-fidelity images, audio, and video by gradually reversing a parameterized noise process.",
            coreConcepts = listOf(
                "Forward Diffusion: Adding Gaussian noise step-by-step until an image turns into pure static.",
                "Reverse Diffusion: Training a U-Net / DiT (Diffusion Transformer) to predict and remove noise at each timestep.",
                "Latent Diffusion: Operating in a compressed latent space (like Stable Diffusion) for computational efficiency.",
                "Classifier-Free Guidance (CFG): Controlling how strictly the image follows the text prompt."
            ),
            tutorialContent = """
                Diffusion models superseded Generative Adversarial Networks (GANs) as the state of the art for media generation (Midjourney, Stable Diffusion, DALL-E, Sora).

                How it works:
                1. Text Encoding: A text prompt is encoded using CLIP or T5 into text embeddings.
                2. Denoising Loop: Starting from pure random Gaussian noise, the model iteratively predicts noise components at timestep t and subtracts them.
                3. VAE Decoder: The denoised latent representation is decoded into the final high-resolution RGB image.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Diffusion_model"
        ),

        AiTopic(
            id = "rlhf",
            title = "Reinforcement Learning from Human Feedback (RLHF)",
            wikiSlug = "Reinforcement_learning_from_human_feedback",
            category = "Safety & Alignment",
            difficulty = "Advanced",
            readTime = "6 min",
            shortSummary = "A post-training alignment method that optimizes model outputs using human preference data to ensure helpful, harmless, and honest behavior.",
            coreConcepts = listOf(
                "Reward Modeling: Training a neural network to score response quality based on human comparison rankings.",
                "PPO (Proximal Policy Optimization): Adjusting LLM weights to maximize reward while penalizing drift via KL divergence.",
                "DPO (Direct Preference Optimization): Modern alternative bypassing the separate reward model to train directly on pairwise preferences.",
                "Alignment Criteria: Balancing helpfulness, truthfulness, and safety constraints."
            ),
            tutorialContent = """
                Raw pre-trained language models simply predict the most likely internet text, which frequently includes misinformation, toxicity, or unhelpful continuations.

                RLHF bridges the gap between raw text prediction and user intent:
                Step 1: Human annotators rank candidate responses (Response A vs Response B).
                Step 2: A Reward Model is trained to output a scalar score predicting human preference.
                Step 3: The language model is fine-tuned via Reinforcement Learning to maximize this reward while maintaining coherence.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Reinforcement_learning_from_human_feedback"
        ),

        AiTopic(
            id = "deep_learning",
            title = "Deep Learning & Neural Networks",
            wikiSlug = "Deep_learning",
            category = "Deep Learning",
            difficulty = "Beginner",
            readTime = "6 min",
            shortSummary = "A subset of machine learning based on multi-layered artificial neural networks capable of learning hierarchical representations from raw data.",
            coreConcepts = listOf(
                "Artificial Neurons (Perceptrons): Weighted sum of inputs passed through a non-linear activation function.",
                "Backpropagation: Calculating gradients of the loss function via the chain rule to update weights.",
                "Optimization Algorithms: Stochastic Gradient Descent (SGD), Adam, and AdamW.",
                "Overfitting & Regularization: Dropout, Weight Decay, and Data Augmentation."
            ),
            tutorialContent = """
                Deep Learning is the core engine behind all modern AI breakthroughs. 'Deep' refers to neural networks with many hidden layers between input and output.

                Each layer learns increasingly abstract representations:
                - Early layers learn simple edges, textures, or word embeddings.
                - Intermediate layers learn parts, motifs, and grammatical structures.
                - Deep layers learn complex semantic concepts, reasoning, and context.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Deep_learning"
        ),

        AiTopic(
            id = "multimodal",
            title = "Multimodal Learning & Vision-Language Models",
            wikiSlug = "Multimodal_learning",
            category = "Vision & Multimodal",
            difficulty = "Intermediate",
            readTime = "6 min",
            shortSummary = "Models that process and reason across multiple modalities simultaneously, connecting text, vision, audio, and code into unified representations.",
            coreConcepts = listOf(
                "Cross-Modal Attention: Allowing textual queries to directly attend to visual patch tokens.",
                "Vision Encoders: Using Vision Transformers (ViT) to tokenize images into visual embeddings.",
                "Contrastive Pre-training (CLIP): Learning shared semantic spaces where matching image-text pairs have high similarity.",
                "Interleaved Reasoning: Generating interleaved text and image responses."
            ),
            tutorialContent = """
                Humans perceive the world multimodally: sight, sound, language, and touch. Multimodal AI bridges the gap between isolated unimodal systems.

                Modern Vision-Language Models (VLMs):
                An image is sliced into a grid of 16x16 patches. A Vision Transformer converts these patches into tokens. A linear projection aligns these visual tokens with the text embedding space of the LLM. The LLM can then 'read' the image as if it were a sequence of words!
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Multimodal_learning"
        ),

        AiTopic(
            id = "vector_db",
            title = "Vector Databases & High-Dimensional Embeddings",
            wikiSlug = "Vector_database",
            category = "Generative AI",
            difficulty = "Intermediate",
            readTime = "5 min",
            shortSummary = "Specialized data storage optimized for indexing and ultra-fast similarity searching across high-dimensional numerical vectors generated by AI models.",
            coreConcepts = listOf(
                "Embeddings: Representing semantic meaning as vectors in 768 to 3072 dimensional space.",
                "HNSW Index: Hierarchical Navigable Small World graphs for fast sub-linear search time.",
                "Distance Metrics: Cosine Similarity, Euclidean Distance (L2), Dot Product.",
                "Metadata Filtering: Combining vector proximity with relational boolean filters."
            ),
            tutorialContent = """
                Traditional databases index strings, numbers, and dates using B-trees or hash tables for exact matches. However, AI understands concepts through semantic similarity.

                Vector databases are purpose-built to index vectors and answer questions like: 'Find the 5 closest documents in conceptual meaning to this query' within milliseconds across millions of items.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Vector_database"
        ),

        AiTopic(
            id = "ai_safety",
            title = "AI Safety, Ethics & Alignment",
            wikiSlug = "AI_safety",
            category = "Safety & Alignment",
            difficulty = "Beginner",
            readTime = "5 min",
            shortSummary = "The multidisciplinary field dedicated to ensuring artificial intelligence systems remain reliable, ethically aligned with human values, and secure from exploitation.",
            coreConcepts = listOf(
                "The Alignment Problem: Aligning AI goals with intended human values without unintended side-effects.",
                "Red Teaming: Adversarial testing to uncover vulnerabilities, jailbreaks, and unsafe outputs.",
                "Mechanistic Interpretability: Reverse-engineering neural network weights to inspect internal reasoning.",
                "Bias & Fairness: Identifying and correcting dataset biases."
            ),
            tutorialContent = """
                As AI models gain reasoning capabilities and autonomy, safety becomes critical. AI safety spans short-term technical challenges (hallucinations, bias, prompt injection) and long-term societal challenges (labor transformation, autonomy oversight, control problems).

                Key research directions include Constitutional AI, automated red-teaming, watermarking synthetic media, and verifiable safety guarantees for autonomous agent operations.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/AI_safety"
        ),

        AiTopic(
            id = "reasoning_models",
            title = "Chain-of-Thought & Reasoning Models",
            wikiSlug = "Chain-of-thought_prompting",
            category = "AI Agents",
            difficulty = "Intermediate",
            readTime = "6 min",
            shortSummary = "Techniques and models trained to generate explicit hidden or visible chains of reasoning before producing conclusions, revolutionizing math and logic benchmarks.",
            coreConcepts = listOf(
                "Test-Time Compute: Allocating more inference tokens to think, verify, and backtrack.",
                "Self-Correction & Verification: Reviewing intermediate calculation steps before finalizing answers.",
                "Process Reward Models: Rewarding each step in the reasoning chain rather than just the final answer.",
                "Tree-of-Thought: Exploring multiple reasoning branches and pruning unpromising paths."
            ),
            tutorialContent = """
                Early language models were trained to answer immediately on the next token. For complex logic, math, or coding, this caused premature mistakes.

                Chain-of-Thought prompting and modern reasoning models (like OpenAI o1/o3, DeepSeek R1) fundamentally shifted the paradigm: by encouraging or training the model to output a 'stream of thought', the model can explore alternative hypotheses, verify logic, correct mistakes, and achieve human-expert performance in competitive programming and Olympiad math.
            """.trimIndent(),
            webUrl = "https://en.wikipedia.org/wiki/Chain-of-thought_prompting"
        )
    )

    private val liveFeedTopics = mutableListOf<AiTopic>()
    private val allTopics = mutableListOf<AiTopic>().apply {
        addAll(foundationTopics)
    }

    @Volatile
    private var isFetching = false

    /**
     * Fetches the latest live AI feeds from external sources (ArXiv, TechCrunch, Hugging Face, etc.)
     * and updates trending topics dynamically.
     */
    fun fetchTrendingFeeds(onComplete: ((Boolean, Int) -> Unit)? = null) {
        if (isFetching) {
            onComplete?.invoke(false, liveFeedTopics.size)
            return
        }
        isFetching = true
        AiFeedService.fetchTrendingFeeds { fetchedTopics, success ->
            isFetching = false
            if (success && fetchedTopics.isNotEmpty()) {
                synchronized(this) {
                    val bookmarkedIds = allTopics.filter { it.isBookmarked }.map { it.id }.toSet()
                    val completedIds = allTopics.filter { it.isCompleted }.map { it.id }.toSet()

                    liveFeedTopics.clear()
                    liveFeedTopics.addAll(fetchedTopics)

                    allTopics.clear()
                    // Dynamic trending feeds appear right at the top!
                    allTopics.addAll(liveFeedTopics)

                    // Merge curriculum foundation topics
                    for (foundation in foundationTopics) {
                        if (allTopics.none { it.id == foundation.id || it.title.equals(foundation.title, ignoreCase = true) }) {
                            allTopics.add(foundation)
                        }
                    }

                    // Restore state
                    for (topic in allTopics) {
                        if (bookmarkedIds.contains(topic.id)) topic.isBookmarked = true
                        if (completedIds.contains(topic.id)) topic.isCompleted = true
                    }
                }
            }
            onComplete?.invoke(success, liveFeedTopics.size)
        }
    }

    fun getAllTopics(): List<AiTopic> = synchronized(this) { allTopics.toList() }

    fun getFoundationTopics(): List<AiTopic> = foundationTopics

    fun getLiveTopics(): List<AiTopic> = synchronized(this) { liveFeedTopics.toList() }

    fun isLiveFeedLoaded(): Boolean = synchronized(this) { liveFeedTopics.isNotEmpty() }

    fun getTrendingSpotlight(): AiTopic? = synchronized(this) {
        liveFeedTopics.firstOrNull() ?: allTopics.find { it.id == "rag" } ?: allTopics.firstOrNull()
    }

    fun getTopicById(id: String): AiTopic? = synchronized(this) {
        allTopics.find { it.id == id }
    }

    fun filterTopics(category: String?, searchQuery: String?): List<AiTopic> = synchronized(this) {
        allTopics.filter { topic ->
            val matchesCategory = when {
                category == null || category == "All" -> true
                category.equals("Live Feeds", ignoreCase = true) || category.equals("Trending Feeds", ignoreCase = true) -> topic.isLiveFeed
                else -> topic.category.equals(category, ignoreCase = true)
            }
            val matchesSearch = searchQuery.isNullOrBlank() ||
                    topic.title.contains(searchQuery, ignoreCase = true) ||
                    topic.shortSummary.contains(searchQuery, ignoreCase = true) ||
                    topic.category.contains(searchQuery, ignoreCase = true) ||
                    topic.sourceName.contains(searchQuery, ignoreCase = true) ||
                    topic.feedTag.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    fun toggleBookmark(id: String): Boolean = synchronized(this) {
        val topic = getTopicById(id) ?: return false
        topic.isBookmarked = !topic.isBookmarked
        topic.isBookmarked
    }

    fun toggleCompleted(id: String): Boolean = synchronized(this) {
        val topic = getTopicById(id) ?: return false
        topic.isCompleted = !topic.isCompleted
        topic.isCompleted
    }

    fun getProgress(): Pair<Int, Int> = synchronized(this) {
        val completed = allTopics.count { it.isCompleted }
        Pair(completed, allTopics.size)
    }

    /**
     * Live fetches Wikipedia summary via official REST API
     * https://en.wikipedia.org/api/rest_v1/page/summary/{wikiSlug}
     */
    fun fetchWikipediaSummary(wikiSlug: String, onResult: (String?) -> Unit) {
        thread {
            try {
                val url = URL("https://en.wikipedia.org/api/rest_v1/page/summary/$wikiSlug")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "AITutorialApp/1.0 (Android; Educational)")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONObject(response)
                    val extract = if (json.has("extract")) json.getString("extract") else null

                    val topic = synchronized(this) { allTopics.find { it.wikiSlug == wikiSlug } }
                    if (extract != null && topic != null) {
                        topic.liveWikiExtract = extract
                    }
                    onResult(extract)
                } else {
                    onResult(null)
                }
            } catch (_: Exception) {
                onResult(null)
            }
        }
    }
}
