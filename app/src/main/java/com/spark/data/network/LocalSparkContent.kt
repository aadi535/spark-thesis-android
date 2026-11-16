package com.spark.data.network

data class QuestionCardItem(
    val domain: String,
    val emoji: String,
    val question: String,
    val tagColor: String
)

/** Bundled guest/offline content only; no network access or credentials. */
object LocalSparkContent {
    // Six questions per existing topic pool; keep the bundled collection bounded.
    private val sciencePool = listOf(
        QuestionCardItem("Science & Cosmos", "🕳️", "Why can light escape a star but not a black hole?", "#38BDF8"),
        QuestionCardItem("Science & Cosmos", "🛰️", "Why do navigation satellites need very accurate clocks?", "#38BDF8"),
        QuestionCardItem("Science & Cosmos", "🐱", "Why did a physicist imagine a cat in a box?", "#38BDF8"),
        QuestionCardItem("Science & Cosmos", "⚡", "How can tiny particles cross a barrier without climbing over it?", "#38BDF8"),
        QuestionCardItem("Science & Cosmos", "🌌", "Why have we not found clear evidence of life beyond Earth?", "#38BDF8"),
        QuestionCardItem("Science & Cosmos", "☀️", "How can activity on the Sun affect technology on Earth?", "#38BDF8")
    )

    private val psychologyPool = listOf(
        QuestionCardItem("Psychology & Mind", "🧠", "Why can it be hard to stop scrolling through a feed?", "#A855F7"),
        QuestionCardItem("Psychology & Mind", "🌙", "Why can dreams feel vivid even when their stories make little sense?", "#A855F7"),
        QuestionCardItem("Psychology & Mind", "🧩", "What can brain injuries teach us about sharing information?", "#A855F7"),
        QuestionCardItem("Psychology & Mind", "🎯", "Why is it sometimes hard to judge our own skills?", "#A855F7"),
        QuestionCardItem("Psychology & Mind", "🎵", "Why can the same piece of music affect people differently?", "#A855F7"),
        QuestionCardItem("Psychology & Mind", "👁️", "How can watching a speaker change what we think we hear?", "#A855F7")
    )

    private val historyPool = listOf(
        QuestionCardItem("Ancient History", "🏛️", "How could ancient gears help people follow the sky?", "#F59E0B"),
        QuestionCardItem("Ancient History", "📜", "Why did ancient Alexandria collect so many books?", "#F59E0B"),
        QuestionCardItem("Ancient History", "🧱", "How did early cities manage water and waste?", "#F59E0B"),
        QuestionCardItem("Ancient History", "⚔️", "How do historians investigate why an ancient society changed?", "#F59E0B"),
        QuestionCardItem("Ancient History", "🗝️", "How did one stone help scholars read an ancient script?", "#F59E0B"),
        QuestionCardItem("Ancient History", "🏺", "How can archaeologists test an unusual idea about an old object?", "#F59E0B")
    )

    private val biologyPool = listOf(
        QuestionCardItem("Biology & Nature", "🧬", "How did bacteria help inspire a tool for editing DNA?", "#10B981"),
        QuestionCardItem("Biology & Nature", "🐙", "How can an octopus coordinate so many flexible arms?", "#10B981"),
        QuestionCardItem("Biology & Nature", "🦠", "How do gut microbes help break down some foods?", "#10B981"),
        QuestionCardItem("Biology & Nature", "🍄", "How do fungi and plant roots help each other?", "#10B981"),
        QuestionCardItem("Biology & Nature", "🩸", "How can cells use the same DNA in different ways?", "#10B981"),
        QuestionCardItem("Biology & Nature", "🪼", "How can some tiny animals survive drying out?", "#10B981")
    )

    private val mathematicsPool = listOf(
        QuestionCardItem("Mathematics & Logic", "📐", "Why do some number collections start with one more often?", "#EC4899"),
        QuestionCardItem("Mathematics & Logic", "🎲", "Why can switching doors help in the Monty Hall game?", "#EC4899"),
        QuestionCardItem("Mathematics & Logic", "♾️", "How can a small change in a question change its answer?", "#EC4899"),
        QuestionCardItem("Mathematics & Logic", "🌀", "How can repeating simple rules create a complex pattern?", "#EC4899"),
        QuestionCardItem("Mathematics & Logic", "🔮", "Why does a map need a scale?", "#EC4899"),
        QuestionCardItem("Mathematics & Logic", "♟️", "Why can a longer password be harder to guess?", "#EC4899")
    )

    private val techPhilosophyPool = listOf(
        QuestionCardItem("Tech & Philosophy", "🚀", "How is a quantum computer different from an ordinary computer?", "#06B6D4"),
        QuestionCardItem("Tech & Philosophy", "🤖", "How can a language model use the words around a word?", "#06B6D4"),
        QuestionCardItem("Tech & Philosophy", "⛵", "Why do philosophers ask whether a repaired object stays the same?", "#06B6D4"),
        QuestionCardItem("Tech & Philosophy", "🕶️", "Why can a convincing story still need evidence?", "#06B6D4"),
        QuestionCardItem("Tech & Philosophy", "🕯️", "How can a good question reveal a hidden assumption?", "#06B6D4"),
        QuestionCardItem("Tech & Philosophy", "☢️", "How can someone prove a claim without revealing every secret?", "#06B6D4")
    )

    fun getDiverseMultidisciplinaryMix(seenQuestions: Set<String> = emptySet()): List<QuestionCardItem> {
        val s = sciencePool.filter { it.question !in seenQuestions }.shuffled().firstOrNull() ?: sciencePool.shuffled().first()
        val p = psychologyPool.filter { it.question !in seenQuestions }.shuffled().firstOrNull() ?: psychologyPool.shuffled().first()
        val h = historyPool.filter { it.question !in seenQuestions }.shuffled().firstOrNull() ?: historyPool.shuffled().first()
        val b = biologyPool.filter { it.question !in seenQuestions }.shuffled().firstOrNull() ?: biologyPool.shuffled().first()
        val m = mathematicsPool.filter { it.question !in seenQuestions }.shuffled().firstOrNull() ?: mathematicsPool.shuffled().first()
        val t = techPhilosophyPool.filter { it.question !in seenQuestions }.shuffled().firstOrNull() ?: techPhilosophyPool.shuffled().first()

        return listOf(s, p, h, b, m, t)
    }

    fun getCuratedHighQualityFallback(topic: String): SparkTaskDto {
        val q = topic.lowercase().trim()
        return when {
            q.contains("black hole") || q.contains("singularity") || q.contains("event horizon") -> {
                SparkTaskDto(
                    category = "Astrophysics & Cosmos",
                    icon = "🕳️",
                    color = "#38BDF8",
                    title = "Why can light escape a star but not a black hole?",
                    hook = "The event horizon marks a limit on what can escape.",
                    description = "A black hole has a boundary called an event horizon. Once light is inside that boundary, it cannot reach distant observers. General relativity describes this in terms of curved spacetime, the combined description of space and time. The theory predicts extreme conditions deeper inside, but we do not have a complete tested description of the centre. A black hole is not a cosmic vacuum cleaner pulling in everything nearby.",
                    steps = listOf("The event horizon marks a limit on what can escape.", "Separate tested observations from uncertain descriptions of the centre."),
                    funFact = "Stars can orbit a black hole without falling into it.",
                    xp = 150,
                    badge = "Cosmic Explorer 🕳️",
                    isAI = false,
                    link = null
                )
            }
            q.contains("dopamine") || q.contains("scroll") || q.contains("habit") || q.contains("tiktok") || q.contains("instagram") -> {
                SparkTaskDto(
                    category = "Behavioral Psychology",
                    icon = "🧠",
                    color = "#A855F7",
                    title = "Why can it be hard to stop scrolling through a feed?",
                    hook = "Notice when checking becomes automatic.",
                    description = "A feed keeps offering new items, some more interesting than others. That changing mix, together with easy repeated actions, can encourage continued checking. Dopamine is involved in learning and motivation, but it is not a simple switch that explains every person's behaviour. Habits, mood, design and circumstances also matter. A pause can help someone notice what they want to do next; it is not a guaranteed treatment.",
                    steps = listOf("Notice when checking becomes automatic.", "A pause creates an opportunity to choose another action."),
                    funFact = "People can respond differently to the same app design.",
                    xp = 150,
                    badge = "Mind Architect 🧠",
                    isAI = false,
                    link = null
                )
            }
            q.contains("antikythera") || q.contains("greek computer") || q.contains("ancient") || q.contains("rome") || q.contains("greece") -> {
                SparkTaskDto(
                    category = "Ancient History",
                    icon = "🏛️",
                    color = "#F59E0B",
                    title = "How could ancient gears help people follow the sky?",
                    hook = "Gears can turn a repeated motion into a useful model.",
                    description = "The Antikythera mechanism used bronze gears to model repeating patterns in the sky. Turning a handle moved linked parts that represented astronomical cycles. Surviving fragments and modern imaging show that it could track calendar information and predict eclipses. Some missing parts are still debated, so reconstructions are interpretations of the evidence rather than complete original instructions.",
                    steps = listOf("Gears can turn a repeated motion into a useful model.", "Missing pieces mean historians must distinguish evidence from reconstruction."),
                    funFact = "The surviving mechanism was recovered from a shipwreck.",
                    xp = 150,
                    badge = "History Master 🏛️",
                    isAI = false,
                    link = null
                )
            }
            q.contains("octopus") || q.contains("tentacle") || q.contains("cephalopod") -> {
                SparkTaskDto(
                    category = "Biology & Genetics",
                    icon = "🐙",
                    color = "#10B981",
                    title = "How can an octopus coordinate so many flexible arms?",
                    hook = "Local nerve networks help flexible arms respond to touch.",
                    description = "An octopus has a central brain and many nerve cells distributed through its arms. Those local nerve networks help process touch and guide movement, rather than sending every detail through one central point. Calling the arms eight extra brains is a memorable shorthand, but it can mislead. The animal's behaviour depends on coordination between central and local nervous systems.",
                    steps = listOf("Local nerve networks help flexible arms respond to touch.", "A useful shorthand is not always a literal description."),
                    funFact = "Octopus suckers help the animal explore surfaces chemically as well as physically.",
                    xp = 150,
                    badge = "Marine Biologist 🐙",
                    isAI = false,
                    link = null
                )
            }
            q.contains("crispr") || q.contains("dna") || q.contains("gene") || q.contains("bio") -> {
                SparkTaskDto(
                    category = "Biology & Genetics",
                    icon = "🧬",
                    color = "#10B981",
                    title = "How did bacteria help inspire a tool for editing DNA?",
                    hook = "A natural defence system inspired a laboratory tool.",
                    description = "Some bacteria keep molecular records of past virus encounters and use them to recognize related threats. Researchers adapted parts of these systems into gene-editing tools such as CRISPR-Cas9. A guide molecule helps direct an enzyme to a matching DNA sequence. Changing DNA reliably remains difficult and can have unintended effects. This explains the principle without giving instructions for an experiment.",
                    steps = listOf("A natural defence system inspired a laboratory tool.", "Targeting a sequence does not guarantee a perfect edit."),
                    funFact = "Different CRISPR systems use different enzymes.",
                    xp = 150,
                    badge = "Bio Engineer 🧬",
                    isAI = false,
                    link = null
                )
            }
            q.contains("benford") || q.contains("math") || q.contains("fraud") || q.contains("number") -> {
                SparkTaskDto(
                    category = "Mathematics & Logic",
                    icon = "📐",
                    color = "#EC4899",
                    title = "Why do some number collections start with one more often?",
                    hook = "Check whether a dataset is suitable before applying a pattern.",
                    description = "Some collections of measurements span a wide range of sizes. In many such collections, smaller leading digits occur more often than larger ones, a pattern called Benford's law. It does not describe every list of numbers. Fixed limits, assigned codes and small samples can change the pattern. An unusual digit pattern can suggest a question worth investigating; it does not prove fraud.",
                    steps = listOf("Check whether a dataset is suitable before applying a pattern.", "An unusual result is a clue, not a verdict."),
                    funFact = "Telephone numbers are assigned identifiers, not a typical use of Benford's law.",
                    xp = 150,
                    badge = "Math Savant 📐",
                    isAI = false,
                    link = null
                )
            }
            q.contains("quantum") || q.contains("computing") || q.contains("qubit") || q.contains("tech") || q.contains("code") -> {
                SparkTaskDto(
                    category = "Tech & Future AI",
                    icon = "🚀",
                    color = "#06B6D4",
                    title = "How is a quantum computer different from an ordinary computer?",
                    hook = "Quantum advantages depend on the problem and algorithm.",
                    description = "Ordinary computers use bits, while quantum computers use physical systems called qubits. Carefully arranged operations use quantum effects to change the chances of different measurement results. Some algorithms may solve particular problems more efficiently. This does not mean a quantum computer reads every possible answer at once or is faster at every task. Noise and errors remain major engineering challenges.",
                    steps = listOf("Quantum advantages depend on the problem and algorithm.", "A measurement does not reveal every possible result."),
                    funFact = "Different quantum-computer designs use different physical systems for qubits.",
                    xp = 150,
                    badge = "Quantum Architect 🚀",
                    isAI = false,
                    link = null
                )
            }
            q.contains("relativity") || q.contains("gps") || q.contains("einstein") || q.contains("time") -> {
                SparkTaskDto(
                    category = "Astrophysics & Space",
                    icon = "🛰️",
                    color = "#38BDF8",
                    title = "Why do satellite navigation systems need very accurate clocks?",
                    hook = "Signal travel time helps estimate distance.",
                    description = "Navigation satellites broadcast timing information. A receiver compares signals from several satellites to estimate its position, so small timing errors matter. Motion and differences in gravity affect the rate of clocks, as described by relativity. Navigation systems account for these effects. This is a practical use of physics, not evidence that a phone can control time.",
                    steps = listOf("Signal travel time helps estimate distance.", "Clock corrections help keep position estimates useful."),
                    funFact = "A receiver also needs to account for its own clock error.",
                    xp = 150,
                    badge = "Time Traveler 🛰️",
                    isAI = false,
                    link = null
                )
            }
            else -> {
                // Pick a random rich spark from the dynamic catalog instead of generic text
                val spark = com.spark.ui.screens.dashboard.DynamicSparkCatalog.catalog.random()
                SparkTaskDto(
                    category = spark.category,
                    icon = spark.emoji,
                    color = "#38BDF8",
                    title = spark.title,
                    hook = spark.steps.firstOrNull() ?: "Explore the fascinating science and history behind this concept.",
                    description = spark.description,
                    steps = spark.steps,
                    funFact = spark.funFact,
                    xp = 150,
                    badge = "Deep Thinker ⚡",
                    isAI = false,
                    link = null
                )
            }
        }
    }

}
