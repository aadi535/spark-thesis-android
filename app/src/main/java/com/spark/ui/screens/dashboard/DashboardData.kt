package com.spark.ui.screens.dashboard

import androidx.compose.ui.graphics.Color

data class MoodItem(val id: String, val emoji: String, val label: String, val desc: String)
data class TimeItem(val id: String, val label: String, val desc: String)
data class MasterySubtopic(val id: String, val label: String, val detail: String)
data class MasterySubject(
    val id: String, 
    val label: String, 
    val color: Color, 
    val levels: List<String>, 
    val subtopicsByLevel: Map<String, List<MasterySubtopic>>
)
data class WonderSubject(val id: String, val label: String, val color: Color, val options: List<String>)

data class SparkSuggestion(
    val category: String,
    val duration: String,
    val title: String,
    val description: String
)

object DashboardConstants {

    val MOODS = listOf(
        MoodItem("lazy", "😴", "Lazy", "Easy & fascinating"),
        MoodItem("energetic", "⚡", "Energetic", "Deep & challenging"),
        MoodItem("curious", "🤔", "Curious", "Weird & unexpected"),
        MoodItem("creative", "🎨", "Creative", "Fun & imaginative")
    )

    val TIMES = listOf(
        TimeItem("15 minutes", "15 min", "Quick spark"),
        TimeItem("30 minutes", "30 min", "Deep dive"),
        TimeItem("1 hour", "1 hour", "Full immersion")
    )

    val INTERESTS = listOf(
        "Science & Space", "Human Psychology", "Ancient History",
        "Mathematics", "Philosophy", "Nature & Biology",
        "Surprise me!", "Technology"
    )

    val MASTERY_SUBJECTS = listOf(
        MasterySubject(
            id = "physics",
            label = "⚡️ Physics",
            color = Color(0xFF3B82F6),
            levels = listOf("Class 10", "Class 11", "Class 12", "Engineering"),
            subtopicsByLevel = mapOf(
                "Class 10" to listOf(
                    MasterySubtopic("reflection", "Reflection of Light", "Spherical Mirrors, Mirror Formula, and Magnification"),
                    MasterySubtopic("refraction", "Refraction of Light", "Snell's Law, Refractive Index, and Lens Formula"),
                    MasterySubtopic("human-eye", "The Human Eye", "Power of Accommodation, Defects of Vision, Prism Dispersion"),
                    MasterySubtopic("electric-current", "Electric Current & Potential", "Ohm's Law and Experimental Verification"),
                    MasterySubtopic("resistance", "Resistance Dynamics", "Factors Affecting Resistance, Series and Parallel Circuits"),
                    MasterySubtopic("electrical-energy", "Electrical Energy", "Heating Effects, Joule's Law, Electric Power"),
                    MasterySubtopic("magnetic-fields", "Magnetic Fields", "Field Lines, Oersted's Experiment, Fields due to Solenoids"),
                    MasterySubtopic("magnetic-forces", "Magnetic Forces", "Force on Conductor, Fleming's Left-Hand Rule"),
                    MasterySubtopic("em-induction-10", "Electromagnetic Induction", "Induced Current, Magnetic Flux, Fleming's Right-Hand Rule"),
                    MasterySubtopic("domestic-circuits", "Domestic Circuits", "Live/Neutral/Earth Wire, Short Circuits, Overloading")
                ),
                "Class 11" to listOf(
                    MasterySubtopic("vectors", "Vector Algebra & Tools", "Dot Product, Cross Product, and Component Resolution"),
                    MasterySubtopic("kinematics", "Kinematics", "Rectilinear Motion, Projectile Motion, Relative Velocity"),
                    MasterySubtopic("dynamics", "Dynamics - Newton's Laws", "Newton's Laws, Inertial Frames, Friction Mechanisms"),
                    MasterySubtopic("circular-motion", "Circular Motion", "Centripetal/Centrifugal Forces, Banking of Roads"),
                    MasterySubtopic("work-energy", "Work, Energy & Power", "Work-Energy Theorem, Conservative vs Non-Conservative Forces"),
                    MasterySubtopic("momentum", "Linear Momentum & Collisions", "Center of Mass, Elastic and Inelastic Impact"),
                    MasterySubtopic("rotational", "Rotational Mechanics", "Torque, Angular Momentum, Moment of Inertia"),
                    MasterySubtopic("gravitation", "Gravitation", "Gravitational Potential, Kepler's Laws, Escape Velocity"),
                    MasterySubtopic("fluid-solid", "Fluid & Solid Mechanics", "Hooke's Law, Pascal's Law, Bernoulli's Principle"),
                    MasterySubtopic("thermo-waves", "Thermodynamics & Waves", "Laws of Thermodynamics, SHM, Wave Superposition")
                ),
                "Class 12" to listOf(
                    MasterySubtopic("electrostatics", "Electrostatics", "Coulomb's Law, Electric Fields, Gauss's Law"),
                    MasterySubtopic("capacitance", "Capacitance & Potential", "Electrostatic Potential, Dielectrics, Energy Storage"),
                    MasterySubtopic("current-elec", "Current Electricity", "Kirchhoff's Laws, Wheatstone Bridge, Potentiometers"),
                    MasterySubtopic("moving-charges", "Moving Charges & Magnetism", "Biot-Savart Law, Ampere's Law, Lorentz Force"),
                    MasterySubtopic("em-induction-12", "Electromagnetic Induction", "Faraday's and Lenz's Laws, Self/Mutual Induction"),
                    MasterySubtopic("ac-circuits", "Alternating Current", "LCR Circuits, Resonance, and Transformers"),
                    MasterySubtopic("em-waves", "Electromagnetic Waves", "Displacement Current and the EM Spectrum"),
                    MasterySubtopic("wave-optics", "Wave Optics", "Huygens' Principle, YDSE, Diffraction, Polarization"),
                    MasterySubtopic("modern-physics", "Modern Physics", "Photoelectric Effect, Bohr's Model, Radioactivity"),
                    MasterySubtopic("semiconductors", "Semiconductor Devices", "p-n Junction, Rectifiers, Logic Gates")
                ),
                "Engineering" to listOf(
                    MasterySubtopic("vector-calc", "Vector Calculus for Fields", "Gradient, Divergence, Curl, Field Theorems"),
                    MasterySubtopic("maxwell", "Maxwell's Electrodynamics", "Maxwell Equations and EM Wave Propagation"),
                    MasterySubtopic("classical-mech", "Advanced Classical Mechanics", "Lagrangian and Hamiltonian Formulations"),
                    MasterySubtopic("quantum-mech", "Quantum Mechanics", "Schrodinger Wave Equation, Particle in a Box"),
                    MasterySubtopic("relativity", "Special Theory of Relativity", "Lorentz Transforms, Time Dilation, Mass-Energy"),
                    MasterySubtopic("stat-mech", "Statistical Mechanics", "Maxwell-Boltzmann, Bose-Einstein, Fermi-Dirac"),
                    MasterySubtopic("solid-state", "Solid State Physics", "Band Theory, Fermi Levels, Hall Effect"),
                    MasterySubtopic("photonics", "Photonics & Fiber Optics", "Laser, Population Inversion, Numerical Aperture")
                )
            )
        ),
        MasterySubject(
            id = "mathematics",
            label = "📐 Mathematics",
            color = Color(0xFFF59E0B),
            levels = listOf("Class 10", "Class 11", "Class 12", "Engineering"),
            subtopicsByLevel = mapOf(
                "Class 10" to listOf(
                    MasterySubtopic("algebra", "Basic Algebra & Progressions", "AP, GP, Polynomials, Quadratic Equations"),
                    MasterySubtopic("coord-geo-10", "Coordinate Geometry", "Distance Formula, Section Formula, Area of Triangle"),
                    MasterySubtopic("geometry", "Euclidean Geometry", "Triangles, Circle Theorems, Tangents and Chords"),
                    MasterySubtopic("trig-intro", "Introductory Trigonometry", "Ratios, Identities, Heights and Distances"),
                    MasterySubtopic("mensuration", "Mensuration", "Surface Areas and Volumes of 3D Solids"),
                    MasterySubtopic("stats-prob-10", "Statistics & Probability", "Mean, Median, Mode, Basic Probability")
                ),
                "Class 11" to listOf(
                    MasterySubtopic("sets-relations", "Sets, Relations & Functions", "Types of Functions, Domain, Range, Composition"),
                    MasterySubtopic("complex-combo", "Advanced Algebra", "Complex Numbers, Permutations, Combinations, Binomial Theorem"),
                    MasterySubtopic("trig-functions", "Trigonometric Functions", "Unit Circle, Identities, Inverse Trig, Equations"),
                    MasterySubtopic("conic-sections", "Conic Sections", "Parabola, Ellipse, Hyperbola, Focus-Directrix"),
                    MasterySubtopic("limits-deriv", "Limits & Derivatives", "Sandwich Theorem, First Principles, Differentiation Rules")
                ),
                "Class 12" to listOf(
                    MasterySubtopic("matrices", "Matrices & Determinants", "Matrix Operations, Inverse, Cramer's Rule"),
                    MasterySubtopic("diff-calc", "Differential Calculus", "Chain Rule, Implicit Differentiation, Applications"),
                    MasterySubtopic("integral-calc", "Integral Calculus", "Integration Techniques, Definite Integrals, Area Under Curves"),
                    MasterySubtopic("diff-equations", "Differential Equations", "Order, Degree, Separation of Variables, Linear DEs"),
                    MasterySubtopic("vectors-3d", "Vectors & 3D Geometry", "Dot/Cross Product, Lines and Planes in Space"),
                    MasterySubtopic("prob-bayes", "Advanced Probability", "Conditional Probability, Bayes' Theorem, Random Variables")
                ),
                "Engineering" to listOf(
                    MasterySubtopic("linear-algebra", "Linear Algebra", "Vector Spaces, Eigenvalues, Eigenvectors, SVD"),
                    MasterySubtopic("multivariate-calc", "Multivariate Calculus", "Partial Derivatives, Multiple Integrals, Jacobians"),
                    MasterySubtopic("vector-calc-eng", "Vector Calculus", "Gradient, Divergence, Curl, Stokes' and Divergence Theorems"),
                    MasterySubtopic("ode-pde", "ODE & PDE", "Laplace Method, Fourier Series, Heat and Wave Equations"),
                    MasterySubtopic("transforms", "Transform Calculus", "Laplace Transform, Fourier Transform, z-Transform"),
                    MasterySubtopic("complex-analysis", "Complex Analysis", "Analytic Functions, Cauchy-Riemann, Residue Theorem"),
                    MasterySubtopic("numerical-stats", "Numerical Methods & Statistics", "Newton-Raphson, Interpolation, Regression, Hypothesis Testing")
                )
            )
        )
    )

    val WONDER_SUBJECTS = listOf(
        WonderSubject("philosophy", "🧘 Philosophy Schools", Color(0xFFA78BFA), listOf("Ancient Indian", "Greek", "Chinese", "Western")),
        WonderSubject("mysteries", "👽 Mystery Vault", Color(0xFFEC4899), listOf("Alien Theories", "Universe Secrets", "Unsolved Crimes")),
        WonderSubject("discoveries", "📜 Discovery Stories", Color(0xFF38BDF8), listOf("Invention Origins", "Scientific Breakthroughs", "Lost Civilizations"))
    )

    val SPARK_SUGGESTIONS = listOf(
        SparkSuggestion(
            category = "HUMAN PSYCHOLOGY",
            duration = "15 MIN",
            title = "Why Your Brain Craves Dopamine",
            description = "Explore the role of dopamine in motivation, reward, and habit formation. Understand your brain's reward system."
        ),
        SparkSuggestion(
            category = "QUANTUM PHYSICS",
            duration = "30 MIN",
            title = "The Observer Effect Explained",
            description = "Discover how the act of observation changes quantum particles. The double-slit experiment will blow your mind."
        ),
        SparkSuggestion(
            category = "ANCIENT HISTORY",
            duration = "15 MIN",
            title = "Secrets of the Indus Valley",
            description = "A civilization more advanced than Egypt and Mesopotamia. Their drainage system was centuries ahead."
        ),
        SparkSuggestion(
            category = "PHILOSOPHY",
            duration = "1 HOUR",
            title = "Stoicism for Modern Life",
            description = "Marcus Aurelius' timeless principles for handling stress, anxiety, and finding inner peace in chaos."
        )
    )
}

data class GeneratedSpark(
    val title: String,
    val content: String,
    val emoji: String,
    val type: String,
    val isAI: Boolean? = null
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

val SampleGeneratedSpark = GeneratedSpark(
    title = "The Fermi Paradox",
    content = "The Fermi Paradox is the discrepancy between the lack of conclusive evidence of advanced extraterrestrial life and the apparently high likelihood of its existence.\n\nAs a 1950 conversation with fellow physicists, Enrico Fermi supposedly asked, 'Where is everybody?'. There are billions of stars in the galaxy that are similar to the Sun, and many of these stars are billions of years older than the Solar system. High probability suggests some may have Earth-like planets, and some might develop intelligent life and interstellar travel.\n\nIf interstellar travel is possible, the galaxy could be traversed in a few million years. So why haven't we seen any aliens?",
    emoji = "👽",
    type = "Deep Dive",
    isAI = false
)
