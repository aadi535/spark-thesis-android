package com.spark.ui.screens.dashboard

import com.spark.ui.screens.main.MindPalaceData

/**
 * Bundled subtopic catalog for Android offline fallback and interception.
 * Short general-audience lessons; claims are deliberately limited where evidence is uncertain.
 */
object DynamicSparkCatalog {

    data class Subtopic(
        val category: String,
        val emoji: String,
        val title: String,
        val description: String,
        val steps: List<String>,
        val funFact: String
    )

    val catalog = listOf(
        // ── 1. PHILOSOPHY & HISTORY ──────────────────────────────
        Subtopic(
            category = "ANCIENT HISTORY",
            emoji = "🏛️",
            title = "How could ancient gears help people follow the sky?",
            description = "The Antikythera mechanism used bronze gears to model repeating patterns in the sky. Turning a handle moved linked parts that represented astronomical cycles. Surviving fragments and modern imaging show that it could track calendar information and predict eclipses. Some missing parts are still debated, so reconstructions are interpretations of the evidence rather than complete original instructions.",
            steps = listOf("Gears can turn a repeated motion into a useful model.", "Missing pieces mean historians must distinguish evidence from reconstruction."),
            funFact = "The surviving mechanism was recovered from a shipwreck."
        ),
        Subtopic(
            category = "PHILOSOPHY",
            emoji = "🏛️",
            title = "Why did Stoic thinkers focus on what we can control?",
            description = "Stoic philosophers distinguished our choices and judgments from events we cannot fully control. The idea was to respond thoughtfully rather than spend all our effort wishing events were different. It does not mean that hardship is imaginary or that people should accept injustice. It is a philosophical approach to living, not a medical treatment or a guarantee of happiness.",
            steps = listOf("Separate your response from events outside your control.", "Philosophical ideas can be useful without being universal rules."),
            funFact = "Marcus Aurelius wrote reflections that are now known as Meditations."
        ),
        Subtopic(
            category = "PHILOSOPHY SCHOOLS",
            emoji = "📜",
            title = "How can a good question reveal a hidden assumption?",
            description = "Socratic questioning examines a belief by asking what it means and what evidence supports it. Someone might say that a decision is fair, then discover that people use different meanings of fairness. Asking for an example or a possible exception can reveal an assumption. The aim is clearer thinking, not winning an argument or proving that every belief is wrong.",
            steps = listOf("Ask what a key word means.", "Look for an example that tests the assumption."),
            funFact = "Much of what we know of Socrates comes from other writers, including Plato."
        ),
        Subtopic(
            category = "ANCIENT HISTORY",
            emoji = "📚",
            title = "Why did ancient Alexandria collect so many books?",
            description = "The rulers of ancient Alexandria supported a major library as part of a centre for scholarship. Bringing texts together helped scholars compare ideas in subjects such as medicine, literature and astronomy. The collection was written on scrolls rather than modern printed books. Its size and final fate are uncertain; it should not be reduced to one confidently told story of a single fire.",
            steps = listOf("A collection lets people compare different accounts.", "Uncertain historical details should remain uncertain."),
            funFact = "Ancient scholars sometimes compared several copies of a text to identify differences."
        ),

        // ── 2. SCIENCE & COSMOS ──────────────────────────────────
        Subtopic(
            category = "ASTROPHYSICS",
            emoji = "🛰️",
            title = "Why do satellite navigation systems need very accurate clocks?",
            description = "Navigation satellites broadcast timing information. A receiver compares signals from several satellites to estimate its position, so small timing errors matter. Motion and differences in gravity affect the rate of clocks, as described by relativity. Navigation systems account for these effects. This is a practical use of physics, not evidence that a phone can control time.",
            steps = listOf("Signal travel time helps estimate distance.", "Clock corrections help keep position estimates useful."),
            funFact = "A receiver also needs to account for its own clock error."
        ),
        Subtopic(
            category = "QUANTUM PHYSICS",
            emoji = "🐱",
            title = "Why did a physicist imagine a cat in a box?",
            description = "Schrodinger's cat is a thought experiment about how quantum descriptions relate to ordinary observations. The imagined cat is linked to a microscopic event whose outcome is uncertain before measurement. The story highlights a problem in interpreting quantum theory; it is not a claim that real pets should be tested this way. Different interpretations explain measurement differently, and the thought experiment does not settle that debate.",
            steps = listOf("A thought experiment tests the meaning of an idea.", "Do not confuse an illustration with a real experiment."),
            funFact = "Schrodinger introduced the cat example to challenge a simple reading of quantum theory."
        ),
        Subtopic(
            category = "ASTROPHYSICS",
            emoji = "🕳️",
            title = "Why can light escape a star but not a black hole?",
            description = "A black hole has a boundary called an event horizon. Once light is inside that boundary, it cannot reach distant observers. General relativity describes this in terms of curved spacetime, the combined description of space and time. The theory predicts extreme conditions deeper inside, but we do not have a complete tested description of the centre. A black hole is not a cosmic vacuum cleaner pulling in everything nearby.",
            steps = listOf("The event horizon marks a limit on what can escape.", "Separate tested observations from uncertain descriptions of the centre."),
            funFact = "Stars can orbit a black hole without falling into it."
        ),
        Subtopic(
            category = "COSMOLOGY",
            emoji = "🌌",
            title = "Why have we not found clear evidence of life beyond Earth?",
            description = "Astronomers have found many planets, but finding a planet is not the same as finding life. We do not yet know how often life begins or how often it becomes detectable across space. Our searches cover limited places, times and kinds of signals. This uncertainty motivates further observation. It does not prove either that we are alone or that other civilizations must exist.",
            steps = listOf("A missing signal has several possible explanations.", "Search results depend on what instruments can detect."),
            funFact = "Scientists also look for possible signs of life in planetary atmospheres."
        ),

        // ── 3. PSYCHOLOGY & MIND ─────────────────────────────────
        Subtopic(
            category = "BEHAVIORAL NEUROSCIENCE",
            emoji = "🧠",
            title = "Why can it be hard to stop scrolling through a feed?",
            description = "A feed keeps offering new items, some more interesting than others. That changing mix, together with easy repeated actions, can encourage continued checking. Dopamine is involved in learning and motivation, but it is not a simple switch that explains every person's behaviour. Habits, mood, design and circumstances also matter. A pause can help someone notice what they want to do next; it is not a guaranteed treatment.",
            steps = listOf("Notice when checking becomes automatic.", "A pause creates an opportunity to choose another action."),
            funFact = "People can respond differently to the same app design."
        ),
        Subtopic(
            category = "NEUROSCIENCE",
            emoji = "🌙",
            title = "Why can dreams feel vivid even when their stories make little sense?",
            description = "Dreams can combine memories, feelings and imagined events in unusual ways. Vivid dreams are often reported after rapid eye movement sleep, called REM sleep, although dreaming also occurs at other times. During REM sleep, activity in many muscles is reduced. Scientists are still studying why we dream and how dreaming relates to memory and emotion; no single explanation accounts for every dream.",
            steps = listOf("Dreaming is not limited to one sleep stage.", "Separate observations about sleep from theories about its purpose."),
            funFact = "Whether a dream is remembered also depends on what happens around waking."
        ),
        Subtopic(
            category = "COGNITIVE PSYCHOLOGY",
            emoji = "🧩",
            title = "What can split-brain studies reveal about how the brain shares information?",
            description = "Some people with severe epilepsy have had the main connection between the brain's two halves surgically divided. Carefully designed tests can then reveal differences in how information reaches language and movement systems. These results help researchers study communication inside the brain. They do not justify a simple claim that every person has two independent minds, or that one eye belongs entirely to one half of the brain.",
            steps = listOf("Experimental conditions matter when interpreting a result.", "Brain functions involve interacting systems rather than two simple personality types."),
            funFact = "Visual information is organized by visual field, not simply by left eye and right eye."
        ),

        // ── 4. TECH & AI ─────────────────────────────────────────
        Subtopic(
            category = "QUANTUM COMPUTING",
            emoji = "💻",
            title = "How is a quantum computer different from an ordinary computer?",
            description = "Ordinary computers use bits, while quantum computers use physical systems called qubits. Carefully arranged operations use quantum effects to change the chances of different measurement results. Some algorithms may solve particular problems more efficiently. This does not mean a quantum computer reads every possible answer at once or is faster at every task. Noise and errors remain major engineering challenges.",
            steps = listOf("Quantum advantages depend on the problem and algorithm.", "A measurement does not reveal every possible result."),
            funFact = "Different quantum-computer designs use different physical systems for qubits."
        ),
        Subtopic(
            category = "CYBERSECURITY",
            emoji = "🛡️",
            title = "How can software affect a machine in the physical world?",
            description = "Many machines use software to interpret sensor readings and control motors or other equipment. If that software or its inputs are compromised, a machine may behave incorrectly. Industrial cybersecurity therefore protects both information and physical processes. This is a reason for careful design and monitoring, not an invitation to interfere with real equipment. Physical safeguards and people remain important too.",
            steps = listOf("Software can influence a machine through its control system.", "Safety measures should not rely on only one defence."),
            funFact = "Some industrial systems must keep running while security problems are investigated."
        ),

        // ── 5. BIOLOGY & GENETICS ────────────────────────────────
        Subtopic(
            category = "BIOLOGY & GENETICS",
            emoji = "🐙",
            title = "How can an octopus coordinate so many flexible arms?",
            description = "An octopus has a central brain and many nerve cells distributed through its arms. Those local nerve networks help process touch and guide movement, rather than sending every detail through one central point. Calling the arms eight extra brains is a memorable shorthand, but it can mislead. The animal's behaviour depends on coordination between central and local nervous systems.",
            steps = listOf("Local nerve networks help flexible arms respond to touch.", "A useful shorthand is not always a literal description."),
            funFact = "Octopus suckers help the animal explore surfaces chemically as well as physically."
        ),
        Subtopic(
            category = "GENETICS & BIOTECH",
            emoji = "🧬",
            title = "How did bacteria help inspire a tool for editing DNA?",
            description = "Some bacteria keep molecular records of past virus encounters and use them to recognize related threats. Researchers adapted parts of these systems into gene-editing tools such as CRISPR-Cas9. A guide molecule helps direct an enzyme to a matching DNA sequence. Changing DNA reliably remains difficult and can have unintended effects. This explains the principle without giving instructions for an experiment.",
            steps = listOf("A natural defence system inspired a laboratory tool.", "Targeting a sequence does not guarantee a perfect edit."),
            funFact = "Different CRISPR systems use different enzymes."
        ),
        Subtopic(
            category = "MICROBIOLOGY",
            emoji = "🐻",
            title = "How can some tiny animals survive drying out?",
            description = "Some tardigrades can survive severe drying by entering a state with greatly reduced activity. Their cells use protective mechanisms that help limit damage until suitable conditions return. This ability varies with species, life stage and conditions. It does not make the animals indestructible: they can still die, and survival in an experiment does not mean they thrive in that environment.",
            steps = listOf("Survival depends on the species and conditions.", "Dormancy is different from active growth."),
            funFact = "Tardigrades are small animals often found in damp habitats such as moss."
        ),
        // ── 6. EXPANDED ANCIENT HISTORY & ARCHAEOLOGY ───────────
        Subtopic(
            category = "ANCIENT HISTORY",
            emoji = "🏛️",
            title = "What can old stone buildings tell us about early communities?",
            description = "Large ancient structures show that people coordinated labour and shared skills. At sites such as Gobekli Tepe, excavated stone enclosures help archaeologists investigate how early communities gathered and organized their lives. Objects and buildings do not reveal every detail of belief or social life. Dates and interpretations can change as new evidence appears, so one site should not be presented as explaining the whole origin of civilization.",
            steps = listOf("Material remains provide clues rather than complete stories.", "Several interpretations may fit incomplete evidence."),
            funFact = "Archaeology combines objects, environmental evidence and careful comparison."
        ),
        Subtopic(
            category = "ANCIENT HISTORY",
            emoji = "🏺",
            title = "Why can some ancient concrete survive for so long?",
            description = "Concrete is a mixture whose ingredients and surroundings affect how it changes over time. Researchers have studied Roman mixtures containing lime and volcanic materials. In some examples, reactions involving water can help fill small cracks. This does not mean every Roman structure repairs itself or every modern concrete is inferior. The useful lesson is that material chemistry and local conditions both matter.",
            steps = listOf("Durability depends on the mixture and environment.", "Laboratory findings should not be applied to every structure."),
            funFact = "Researchers study surviving materials to understand how they were made."
        ),
        Subtopic(
            category = "ANCIENT HISTORY",
            emoji = "📜",
            title = "How did one stone help scholars read an ancient script?",
            description = "The Rosetta Stone contains related text written in several scripts, including Greek and Egyptian hieroglyphs. Scholars who could read Greek used comparisons to help investigate the other writing. The work involved many clues and researchers rather than one instant solution. The stone helped show how signs could represent sounds as well as ideas.",
            steps = listOf("Related versions of a text provide clues.", "Deciphering a script requires testing those clues elsewhere."),
            funFact = "A script is a writing system; it is not the same thing as a language."
        ),

        // ── 7. EXPANDED ASTROPHYSICS & COSMOLOGY ─────────────────
        Subtopic(
            category = "ASTROPHYSICS",
            emoji = "🌌",
            title = "Why can astronomers still detect light from the early universe?",
            description = "The early universe was hot and dense, and light could not travel far without interacting with matter. As it expanded and cooled, light could travel more freely. That old radiation has been stretched by expansion and is now detected mainly as microwaves. Scientists call it the cosmic microwave background. It is evidence used to test models of cosmic history, not a photograph of the first instant.",
            steps = listOf("Expansion stretches the wavelength of light.", "Measurements can test models of the early universe."),
            funFact = "The microwave background is observed across the sky."
        ),
        Subtopic(
            category = "ASTROPHYSICS",
            emoji = "⏱️",
            title = "How can distant collisions make tiny changes in measured distance?",
            description = "General relativity predicts that changes in gravity can travel as waves. A collision involving very compact objects can produce gravitational waves that reach Earth. Detectors compare the travel of laser light along long arms to look for extremely small changes. Many possible disturbances must be ruled out before a signal is attributed to a distant event.",
            steps = listOf("A detector measures an effect rather than seeing the collision directly.", "Independent checks help distinguish a signal from noise."),
            funFact = "Gravitational waves and ordinary light provide different kinds of astronomical evidence."
        ),
        Subtopic(
            category = "ASTROPHYSICS",
            emoji = "🪐",
            title = "How can a star leave behind something so small and dense?",
            description = "When some massive stars exhaust their fuel, their cores collapse. Under suitable conditions the remnant becomes a neutron star, with much of its matter packed into a very small space. Understanding matter at these densities is still an active area of research. Some rotating neutron stars produce beams that we detect as repeated pulses, rather like a lighthouse sweeping past a distant observer.",
            steps = listOf("Not every dying star leaves the same kind of remnant.", "The lighthouse comparison describes a changing viewing direction."),
            funFact = "A pulsar is a neutron star detected through repeated pulses of radiation."
        ),

        // ── 8. EXPANDED NEUROSCIENCE & PSYCHOLOGY ───────────────
        Subtopic(
            category = "NEUROSCIENCE",
            emoji = "🧠",
            title = "Why does your mind keep working when you are daydreaming?",
            description = "The brain remains active even when attention is not fixed on an outside task. Researchers observe interacting networks associated with remembering, imagining and thinking about ourselves. One is often called the default mode network. These findings do not mean one network controls every wandering thought or that daydreaming diagnoses a mental-health condition. Different tasks and people show different patterns.",
            steps = listOf("Not focusing on a task is not the same as brain inactivity.", "A brain-imaging association does not establish a single cause."),
            funFact = "Daydreaming can involve both remembered experiences and imagined futures."
        ),
        Subtopic(
            category = "PSYCHOLOGY",
            emoji = "⚡",
            title = "Why can an unfinished task keep coming back to mind?",
            description = "An unfinished goal may remain important to us and draw attention later. Experiments on interrupted tasks helped motivate what is often called the Zeigarnik effect. Its strength depends on circumstances, and it is not a universal memory rule. A cliffhanger can also hold attention for several reasons. Making a practical plan may help some people, but no simple technique guarantees that a thought will disappear.",
            steps = listOf("An interesting psychological effect can depend on context.", "One explanation need not account for every example."),
            funFact = "Memory for an event depends on attention, meaning and what happens afterward."
        ),
        Subtopic(
            category = "NEUROSCIENCE",
            emoji = "🌿",
            title = "How can practice change the way the brain works?",
            description = "Learning can change the strength and organization of connections between nerve cells. This capacity for change is called neuroplasticity. Practice, feedback and rest can help develop a skill, but changes vary with the activity and person. A scan showing a difference between groups does not prove that one habit caused every difference, and a single lesson does not guarantee a measurable improvement in intelligence.",
            steps = listOf("Practice can strengthen useful patterns of activity.", "Plasticity does not mean every change is beneficial or unlimited."),
            funFact = "Learning involves interacting systems, not a separate storage box for each fact."
        ),

        // ── 9. EXPANDED BIOLOGY & GENETICS ──────────────────────
        Subtopic(
            category = "BIOLOGY",
            emoji = "🍄",
            title = "How do fungi and plant roots help each other?",
            description = "Many fungi grow in association with plant roots. In these partnerships, fungi can help plants obtain nutrients while receiving carbon compounds made by the plant. Some fungal growth can connect more than one plant. Claims that forests act like an intelligent internet, or that trees routinely send targeted warnings and gifts, go beyond what is firmly established across natural forests.",
            steps = listOf("A partnership can exchange resources without human-like intentions.", "Evidence from one experiment may not describe every forest."),
            funFact = "The word mycorrhiza refers to an association between a fungus and a root."
        ),
        Subtopic(
            category = "BIOLOGY",
            emoji = "🧬",
            title = "How can cells use the same DNA in different ways?",
            description = "Many cells share the same DNA sequence but use different genes. Chemical modifications to DNA and its associated proteins can influence how accessible parts of that sequence are. These processes are studied in epigenetics. Development and environment can matter, but this does not mean a chosen habit can switch any gene on demand or that acquired changes are routinely inherited by later generations.",
            steps = listOf("Gene activity is different from changing the DNA letters.", "Links between environment and gene activity need careful interpretation."),
            funFact = "A skin cell and a nerve cell use different combinations of genes."
        ),

        // ── 10. EXPANDED TECH & COMPUTING ───────────────────────
        Subtopic(
            category = "TECH",
            emoji = "🤖",
            title = "How can a language model use the words around a word?",
            description = "A language model turns text into small units called tokens. Transformer models use a method called attention to weigh relationships between tokens when processing text. Training adjusts the model using many examples. This helps it predict useful continuations, but a fluent continuation is not proof that its claims are true. Understanding the architecture does not remove the need to check important answers.",
            steps = listOf("Context can change how a word is interpreted.", "Text prediction does not itself verify a fact."),
            funFact = "A token may be a word, part of a word or a punctuation mark."
        ),
        Subtopic(
            category = "TECH",
            emoji = "🔐",
            title = "How can someone prove a claim without revealing every secret?",
            description = "In cryptography, a zero-knowledge proof lets a verifier check a particular claim without learning the secret used to support it. The guarantee depends on the precise protocol and assumptions. An analogy is showing that a puzzle has a solution while hiding the solution itself, although real systems use carefully designed mathematics. Such proofs do not make every part of an application automatically private or secure.",
            steps = listOf("A proof has a defined claim and assumptions.", "Privacy for one step does not guarantee privacy for a whole system."),
            funFact = "Cryptographers distinguish proving knowledge from simply revealing information."
        )
    )

    fun getUniqueSparkForTopic(topic: String?, seenTitles: List<String>): GeneratedSpark {
        val filtered = when {
            topic == null || topic.contains("surprise", true) -> catalog
            topic.contains("history", true) || topic.contains("philosophy", true) || topic.contains("ancient", true) ->
                catalog.filter { it.category.contains("PHILOSOPHY", true) || it.category.contains("HISTORY", true) }
            topic.contains("science", true) || topic.contains("cosmos", true) || topic.contains("physics", true) ->
                catalog.filter { it.category.contains("PHYSICS", true) || it.category.contains("SCIENCE", true) || it.category.contains("ASTROPHYSICS", true) || it.category.contains("COSMOLOGY", true) }
            topic.contains("psych", true) || topic.contains("mind", true) || topic.contains("brain", true) ->
                catalog.filter { it.category.contains("NEUROSCIENCE", true) || it.category.contains("PSYCHOLOGY", true) }
            topic.contains("tech", true) || topic.contains("ai", true) || topic.contains("computer", true) ->
                catalog.filter { it.category.contains("TECH", true) || it.category.contains("COMPUTING", true) || it.category.contains("CYBERSECURITY", true) }
            topic.contains("bio", true) || topic.contains("gene", true) ->
                catalog.filter { it.category.contains("BIOLOGY", true) || it.category.contains("GENETICS", true) || it.category.contains("MICROBIOLOGY", true) }
            else -> catalog
        }.ifEmpty { catalog }

        // Filter out previously seen titles to keep discoveries fresh
        val unseen = filtered.filter { sub -> !seenTitles.any { it.equals(sub.title, ignoreCase = true) } }
        val chosen = (unseen.ifEmpty { filtered }).random()

        val formattedSteps = chosen.steps.joinToString("\n- ", prefix = "- ")
        return GeneratedSpark(
            title = chosen.title,
            content = "${chosen.description}\n\nSteps:\n$formattedSteps\n\nFun Fact: ${chosen.funFact}",
            type = chosen.category,
            emoji = chosen.emoji,
            isAI = false
        )
    }
}
