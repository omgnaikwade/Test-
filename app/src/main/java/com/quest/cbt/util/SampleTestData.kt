package com.quest.cbt.util

import com.quest.cbt.data.model.OptionJson
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.data.model.TestJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SampleTestData {

    val sampleJson: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        }
    }

    private const val D = "$"

    fun getSampleTest(): TestJson {
        val questions = mutableListOf<QuestionJson>()

        // ------------------ PHYSICS (Q1 - Q25) ------------------
        // Section A: MCQs (Q1 - Q20)
        questions.add(
            QuestionJson(
                id = 1,
                subject = "Physics",
                section = "A",
                type = "MCQ",
                text = "A particle of mass ${D}m${D} moves in a circular orbit of radius ${D}r${D} under an attractive central force ${D}F = -\\frac{k}{r^2}${D}. The total mechanical energy of the particle is:",
                images = emptyList(),
                options = listOf(
                    OptionJson(text = "${D}-\\frac{k}{2r}${D}"),
                    OptionJson(text = "${D}-\\frac{k}{r}${D}"),
                    OptionJson(text = "${D}\\frac{k}{2r}${D}"),
                    OptionJson(text = "${D}\\frac{k}{r}${D}")
                ),
                answer = "A",
                tolerance = 0.01,
                solution = "For circular motion, centripetal force is ${D}\\frac{mv^2}{r} = \\frac{k}{r^2} \\implies mv^2 = \\frac{k}{r}${D}. Hence kinetic energy ${D}K = \\frac{1}{2}mv^2 = \\frac{k}{2r}${D}. Potential energy ${D}U = -\\int_\\infty^r \\left(-\\frac{k}{x^2}\\right)dx = -\\frac{k}{r}${D}. Total energy ${D}E = K + U = \\frac{k}{2r} - \\frac{k}{r} = -\\frac{k}{2r}${D}."
            )
        )

        questions.add(
            QuestionJson(
                id = 2,
                subject = "Physics",
                section = "A",
                type = "MCQ",
                text = "Two identical capacitors of capacitance ${D}C${D} are connected in parallel and charged to potential ${D}V${D}. They are disconnected from the source and connected in series with a dielectric slab of constant ${D}K = 3${D} inserted into one. The potential difference across the combination is:",
                images = emptyList(),
                options = listOf(
                    OptionJson(text = "${D}\\frac{4}{3}V${D}"),
                    OptionJson(text = "${D}\\frac{8}{3}V${D}"),
                    OptionJson(text = "${D}2V${D}"),
                    OptionJson(text = "${D}\\frac{5}{3}V${D}")
                ),
                answer = "B",
                tolerance = 0.01,
                solution = "Initial charge on each capacitor is ${D}Q_0 = CV${D}. When one capacitor has dielectric ${D}K=3${D}, its capacitance becomes ${D}C_1 = 3C${D} and the other remains ${D}C_2 = C${D}. In series, voltages add up: ${D}V_1 = \\frac{Q_0}{3C} = \\frac{V}{3}${D} and ${D}V_2 = \\frac{Q_0}{C} = V${D}. Re-connected in opposite polarity gives ${D}V_{net} = \\frac{8}{3}V${D}."
            )
        )

        questions.add(
            QuestionJson(
                id = 3,
                subject = "Physics",
                section = "A",
                type = "MCQ",
                text = "An electromagnetic wave with electric field ${D}\\vec{E} = E_0 \\hat{j} \\cos(kz - \\omega t)${D} propagates in free space. The corresponding magnetic field ${D}\\vec{B}${D} is given by:",
                images = emptyList(),
                options = listOf(
                    OptionJson(text = "${D}\\frac{E_0}{c} \\hat{i} \\cos(kz - \\omega t)${D}"),
                    OptionJson(text = "${D}-\\frac{E_0}{c} \\hat{i} \\cos(kz - \\omega t)${D}"),
                    OptionJson(text = "${D}\\frac{E_0}{c} \\hat{k} \\sin(kz - \\omega t)${D}"),
                    OptionJson(text = "${D}-\\frac{E_0}{c} \\hat{k} \\cos(kz - \\omega t)${D}")
                ),
                answer = "B",
                tolerance = 0.01,
                solution = "Direction of propagation is ${D}\\hat{k}${D} (along +z). Poynting vector requires ${D}\\hat{E} \\times \\hat{B} = \\hat{k}${D}. Since ${D}\\hat{E} = \\hat{j}${D}, we must have ${D}\\hat{j} \\times (-\\hat{i}) = \\hat{k}${D}. Therefore, ${D}\\vec{B} = -\\frac{E_0}{c}\\hat{i}\\cos(kz - \\omega t)${D}."
            )
        )

        questions.add(
            QuestionJson(
                id = 4,
                subject = "Physics",
                section = "A",
                type = "MCQ",
                text = "Due to a printing error in the official answer key, this question on thermodynamics adiabatic bulk modulus was declared as BONUS by NTA:",
                images = emptyList(),
                options = listOf(
                    OptionJson(text = "${D}\\gamma P${D}"),
                    OptionJson(text = "${D}P${D}"),
                    OptionJson(text = "${D}\\frac{P}{\\gamma}${D}"),
                    OptionJson(text = "${D}\\gamma^2 P${D}")
                ),
                answer = "BONUS",
                tolerance = 0.01,
                solution = "This question was declared BONUS by NTA. Full marks (+4) are awarded to all candidates."
            )
        )

        // Generate Q5 - Q20
        for (i in 5..20) {
            questions.add(
                QuestionJson(
                    id = i,
                    subject = "Physics",
                    section = "A",
                    type = "MCQ",
                    text = "Physics Question $i: A monochromatic ray of wavelength ${D}\\lambda = 600\\text{ nm}${D} undergoes refraction through a thin prism of angle ${D}A = 6^\\circ${D}. If the refractive index ${D}\\mu = 1.5${D}, find deviation ${D}\\delta = (\\mu - 1)A${D}:",
                    images = emptyList(),
                    options = listOf(
                        OptionJson(text = "${D}3.0^\\circ${D}"),
                        OptionJson(text = "${D}4.5^\\circ${D}"),
                        OptionJson(text = "${D}1.5^\\circ${D}"),
                        OptionJson(text = "${D}6.0^\\circ${D}")
                    ),
                    answer = if (i % 4 == 0) "A" else if (i % 4 == 1) "B" else if (i % 4 == 2) "C" else "D",
                    tolerance = 0.01,
                    solution = "Using ${D}\\delta = (\\mu - 1)A = (1.5 - 1) \\times 6^\\circ = 3.0^\\circ${D}."
                )
            )
        }

        // Section B: Numerical (Q21 - Q25)
        questions.add(
            QuestionJson(
                id = 21,
                subject = "Physics",
                section = "B",
                type = "NUM",
                text = "A body of mass 2 kg is dropped from a height ${D}h = 20\\text{ m}${D}. Taking ${D}g = 10\\text{ m/s}^2${D}, find its speed (in m/s) just before striking the ground:",
                images = emptyList(),
                options = emptyList(),
                answer = "20",
                tolerance = 0.05,
                solution = "${D}v = \\sqrt{2gh} = \\sqrt{2 \\times 10 \\times 20} = \\sqrt{400} = 20\\text{ m/s}${D}."
            )
        )
        questions.add(
            QuestionJson(
                id = 22,
                subject = "Physics",
                section = "B",
                type = "NUM",
                text = "The de Broglie wavelength of an electron accelerated through potential difference ${D}V = 100\\text{ V}${D} is given by ${D}\\lambda = \\frac{1.227}{\\sqrt{V}}\\text{ nm}${D}. Find ${D}\\lambda${D} in Angstroms (enter numeric value, e.g. 1.23):",
                images = emptyList(),
                options = emptyList(),
                answer = "1.23",
                tolerance = 0.05,
                solution = "${D}\\lambda = \\frac{1.227}{\\sqrt{100}}\\text{ nm} = 0.1227\\text{ nm} = 1.227\\text{ \\AA} \\approx 1.23\\text{ \\AA}${D}."
            )
        )
        questions.add(
            QuestionJson(
                id = 23,
                subject = "Physics",
                section = "B",
                type = "NUM",
                text = "In a Young's double slit experiment, fringe width ${D}\\beta = 2\\text{ mm}${D}. The separation between slits is ${D}d = 0.5\\text{ mm}${D} and screen distance is ${D}D = 2\\text{ m}${D}. Find the wavelength of light in nm (integer value, e.g. 500):",
                images = emptyList(),
                options = emptyList(),
                answer = "500",
                tolerance = 1.0,
                solution = "${D}\\lambda = \\frac{\\beta d}{D} = \\frac{(2 \\times 10^{-3})(0.5 \\times 10^{-3})}{2} = 5 \\times 10^{-7}\\text{ m} = 500\\text{ nm}${D}."
            )
        )
        questions.add(
            QuestionJson(
                id = 24,
                subject = "Physics",
                section = "B",
                type = "NUM",
                text = "The half-life of a radioactive nucleus is ${D}T_{1/2} = 20\\text{ days}${D}. The time taken (in days) for its activity to decrease to 1/16th of original value is:",
                images = emptyList(),
                options = emptyList(),
                answer = "80",
                tolerance = 0.1,
                solution = "Since 1/16 = (1/2)^4, 4 half-lives are needed. Total time ${D}t = 4 \\times 20 = 80\\text{ days}${D}."
            )
        )
        questions.add(
            QuestionJson(
                id = 25,
                subject = "Physics",
                section = "B",
                type = "NUM",
                text = "An ideal gas undergoes adiabatic expansion from volume ${D}V_1 = 1\\text{ L}${D} to ${D}V_2 = 32\\text{ L}${D}. If ${D}\\gamma = 1.4 = 7/5${D} and initial temperature ${D}T_1 = 400\\text{ K}${D}, find the final temperature ${D}T_2${D} (in K):",
                images = emptyList(),
                options = emptyList(),
                answer = "100",
                tolerance = 1.0,
                solution = "${D}T_1 V_1^{\\gamma-1} = T_2 V_2^{\\gamma-1} \\implies T_2 = 400 \\times (1/32)^{2/5} = 400 \\times (1/4) = 100\\text{ K}${D}."
            )
        )

        // ------------------ CHEMISTRY (Q26 - Q50) ------------------
        questions.add(
            QuestionJson(
                id = 26,
                subject = "Chemistry",
                section = "A",
                type = "MCQ",
                text = "According to Molecular Orbital Theory, which of the following species is paramagnetic and has a bond order of 2.5?",
                images = emptyList(),
                options = listOf(
                    OptionJson(text = "${D}O_2^+${D}"),
                    OptionJson(text = "${D}N_2^+${D}"),
                    OptionJson(text = "${D}O_2^{2-}${D}"),
                    OptionJson(text = "${D}C_2${D}")
                ),
                answer = "A",
                tolerance = 0.01,
                solution = "For ${D}O_2^+${D} (15 electrons), configuration has one unpaired electron in anti-bonding pi orbital. Bond order = (10 - 5)/2 = 2.5. It is paramagnetic."
            )
        )

        questions.add(
            QuestionJson(
                id = 27,
                subject = "Chemistry",
                section = "A",
                type = "MCQ",
                text = "The IUPAC name of the coordination complex ${D}[Co(NH_3)_5(CO_3)]Cl${D} is:",
                images = emptyList(),
                options = listOf(
                    OptionJson(text = "Pentaamminecarbonatocobalt(III) chloride"),
                    OptionJson(text = "Carbonatopentaamminecobalt(II) chloride"),
                    OptionJson(text = "Pentaamminecarbonatocobalt(II) chloride"),
                    OptionJson(text = "Pentaamminechlorocobalt(III) carbonate")
                ),
                answer = "A",
                tolerance = 0.01,
                solution = "The ligand NH3 is ammine and CO3(2-) is carbonato. Alphabetically ammine comes first. Cobalt oxidation state is +3. Hence, Pentaamminecarbonatocobalt(III) chloride."
            )
        )

        for (i in 28..45) {
            questions.add(
                QuestionJson(
                    id = i,
                    subject = "Chemistry",
                    section = "A",
                    type = "MCQ",
                    text = "Chemistry Question $i: For the reaction ${D}A(g) \\rightleftharpoons B(g) + C(g)${D}, if degree of dissociation is ${D}\\alpha = 0.2${D} at total pressure ${D}P = 1\\text{ atm}${D}, the equilibrium constant ${D}K_p${D} in atm is:",
                    images = emptyList(),
                    options = listOf(
                        OptionJson(text = "${D}0.0416\\text{ atm}${D}"),
                        OptionJson(text = "${D}0.0825\\text{ atm}${D}"),
                        OptionJson(text = "${D}0.125\\text{ atm}${D}"),
                        OptionJson(text = "${D}0.250\\text{ atm}${D}")
                    ),
                    answer = if (i % 3 == 0) "A" else if (i % 3 == 1) "B" else "C",
                    tolerance = 0.01,
                    solution = "${D}K_p = \\frac{\\alpha^2 P}{1 - \\alpha^2} = \\frac{0.04}{0.96} \\approx 0.0416\\text{ atm}${D}."
                )
            )
        }

        // Chemistry Section B: NUM (Q46 - Q50)
        questions.add(
            QuestionJson(
                id = 46,
                subject = "Chemistry",
                section = "B",
                type = "NUM",
                text = "The oxidation state of chromium in ${D}CrO_5${D} (having butterfly structure with two peroxo linkages) is:",
                images = emptyList(),
                options = emptyList(),
                answer = "6",
                tolerance = 0.1,
                solution = "${D}CrO_5${D} contains one oxo oxygen (-2) and four peroxo oxygens (each -1). Oxidation state = +6."
            )
        )
        questions.add(
            QuestionJson(
                id = 47,
                subject = "Chemistry",
                section = "B",
                type = "NUM",
                text = "The number of unpaired electrons in ${D}[Fe(H_2O)_6]^{2+}$${D} is (atomic number of Fe = 26):",
                images = emptyList(),
                options = emptyList(),
                answer = "4",
                tolerance = 0.1,
                solution = "Fe(2+) has 3d6 configuration. Water is a weak field ligand, so high spin complex with 4 unpaired electrons."
            )
        )
        questions.add(
            QuestionJson(
                id = 48,
                subject = "Chemistry",
                section = "B",
                type = "NUM",
                text = "The rate constant of a first order reaction is ${D}k = 6.93 \\times 10^{-3}\\text{ s}^{-1}${D}. The half life of the reaction (in seconds) is:",
                images = emptyList(),
                options = emptyList(),
                answer = "100",
                tolerance = 1.0,
                solution = "${D}t_{1/2} = \\frac{0.693}{k} = \\frac{0.693}{6.93 \\times 10^{-3}} = 100\\text{ seconds}${D}."
            )
        )
        questions.add(
            QuestionJson(
                id = 49,
                subject = "Chemistry",
                section = "B",
                type = "NUM",
                text = "Find the pH of a 0.001 M HCl aqueous solution at 25 degrees Celsius:",
                images = emptyList(),
                options = emptyList(),
                answer = "3",
                tolerance = 0.05,
                solution = "${D}[H^+] = 10^{-3}\\text{ M} \\implies pH = -\\log_{10}(10^{-3}) = 3${D}."
            )
        )
        questions.add(
            QuestionJson(
                id = 50,
                subject = "Chemistry",
                section = "B",
                type = "NUM",
                text = "The total number of stereoisomers for Tartaric acid (HOOC-CH(OH)-CH(OH)-COOH) is:",
                images = emptyList(),
                options = emptyList(),
                answer = "3",
                tolerance = 0.1,
                solution = "Tartaric acid has two chiral centers with symmetry: d, l (enantiomers) and 1 meso form. Total stereoisomers = 3."
            )
        )

        // ------------------ MATHEMATICS (Q51 - Q75) ------------------
        questions.add(
            QuestionJson(
                id = 51,
                subject = "Maths",
                section = "A",
                type = "MCQ",
                text = "The value of the definite integral ${D}\\int_0^{\\pi/2} \\frac{\\sin^{2026}(x)}{\\sin^{2026}(x) + \\cos^{2026}(x)} dx${D} is:",
                images = emptyList(),
                options = listOf(
                    OptionJson(text = "${D}\\frac{\\pi}{4}${D}"),
                    OptionJson(text = "${D}\\frac{\\pi}{2}${D}"),
                    OptionJson(text = "${D}\\pi${D}"),
                    OptionJson(text = "${D}0${D}")
                ),
                answer = "A",
                tolerance = 0.01,
                solution = "Using King's property, ${D}2I = \\int_0^{\\pi/2} 1 dx = \\frac{\\pi}{2} \\implies I = \\frac{\\pi}{4}${D}."
            )
        )

        questions.add(
            QuestionJson(
                id = 52,
                subject = "Maths",
                section = "A",
                type = "MCQ",
                text = "If ${D}A = \\begin{pmatrix} 1 & 2 \\\\ 0 & 1 \\end{pmatrix}${D}, then the matrix ${D}A^n${D} is given by:",
                images = emptyList(),
                options = listOf(
                    OptionJson(text = "${D}\\begin{pmatrix} 1 & 2n \\\\ 0 & 1 \\end{pmatrix}${D}"),
                    OptionJson(text = "${D}\\begin{pmatrix} 1 & 2^n \\\\ 0 & 1 \\end{pmatrix}${D}"),
                    OptionJson(text = "${D}\\begin{pmatrix} n & 2n \\\\ 0 & n \\end{pmatrix}${D}"),
                    OptionJson(text = "${D}\\begin{pmatrix} 1 & n^2 \\\\ 0 & 1 \\end{pmatrix}${D}")
                ),
                answer = "A",
                tolerance = 0.01,
                solution = "By induction, ${D}A = I + N${D} where ${D}N = \\begin{pmatrix} 0 & 2 \\\\ 0 & 0 \\end{pmatrix}${D}. Since ${D}N^2 = 0${D}, ${D}A^n = I + nN = \\begin{pmatrix} 1 & 2n \\\\ 0 & 1 \\end{pmatrix}${D}."
            )
        )

        for (i in 53..70) {
            questions.add(
                QuestionJson(
                    id = i,
                    subject = "Maths",
                    section = "A",
                    type = "MCQ",
                    text = "Mathematics Question $i: If the vectors ${D}\\vec{a} = 2\\hat{i} + \\hat{j} + \\hat{k}${D} and ${D}\\vec{b} = \\hat{i} - 2\\hat{j} + \\lambda\\hat{k}${D} are mutually perpendicular, then the scalar ${D}\\lambda${D} equals:",
                    images = emptyList(),
                    options = listOf(
                        OptionJson(text = "${D}0${D}"),
                        OptionJson(text = "${D}1${D}"),
                        OptionJson(text = "${D}-1${D}"),
                        OptionJson(text = "${D}2${D}")
                    ),
                    answer = "A",
                    tolerance = 0.01,
                    solution = "Since vectors are perpendicular, ${D}\\vec{a} \\cdot \\vec{b} = 0 \\implies 2(1) + 1(-2) + 1(\\lambda) = 0 \\implies \\lambda = 0${D}."
                )
            )
        }

        // Maths Section B: NUM (Q71 - Q75)
        questions.add(
            QuestionJson(
                id = 71,
                subject = "Maths",
                section = "B",
                type = "NUM",
                text = "The area bounded by the parabola ${D}y^2 = 4x${D} and the line ${D}y = x${D} is ${D}\\frac{A}{3}${D}. Enter the value of A (integer):",
                images = emptyList(),
                options = emptyList(),
                answer = "8",
                tolerance = 0.1,
                solution = "Intersection points: x = 0, 4. Area = ${D}\\int_0^4 (2\\sqrt{x} - x)dx = \\frac{32}{3} - 8 = \\frac{8}{3}${D}. Therefore A = 8."
            )
        )
        questions.add(
            QuestionJson(
                id = 72,
                subject = "Maths",
                section = "B",
                type = "NUM",
                text = "The number of real solutions of the equation ${D}e^x + \\sin(x) = 0${D} in the interval (-infinity, 0) is:",
                images = emptyList(),
                options = emptyList(),
                answer = "1",
                tolerance = 0.1,
                solution = "As ${D}x \\to -\\infty${D}, ${D}e^x \\to 0${D}. There is a unique root in the interval."
            )
        )
        questions.add(
            QuestionJson(
                id = 73,
                subject = "Maths",
                section = "B",
                type = "NUM",
                text = "If ${D}\\lim_{x \\to 0} \\frac{\\sin(ax) - \\sin(bx)}{x} = 5${D} and ${D}a + b = 9${D}, then the value of a (given a > b) is:",
                images = emptyList(),
                options = emptyList(),
                answer = "7",
                tolerance = 0.1,
                solution = "The limit gives ${D}a - b = 5${D}. Since ${D}a + b = 9${D}, ${D}2a = 14 \\implies a = 7${D}."
            )
        )
        questions.add(
            QuestionJson(
                id = 74,
                subject = "Maths",
                section = "B",
                type = "NUM",
                text = "The eccentric angle of a point on ellipse ${D}\\frac{x^2}{16} + \\frac{y^2}{9} = 1${D} whose distance from origin is ${D}\\sqrt{12}${D} satisfies ${D}\\cos^2(\\theta) = \\frac{3}{7} \\approx 0.43${D}. Enter 43:",
                images = emptyList(),
                options = emptyList(),
                answer = "43",
                tolerance = 1.0,
                solution = "${D}r^2 = 16\\cos^2\\theta + 9\\sin^2\\theta = 12 \\implies 7\\cos^2\\theta = 3 \\implies \\cos^2\\theta = 3/7 \\approx 0.43${D}."
            )
        )
        questions.add(
            QuestionJson(
                id = 75,
                subject = "Maths",
                section = "B",
                type = "NUM",
                text = "The radius of the sphere ${D}x^2 + y^2 + z^2 - 2x + 4y - 6z - 11 = 0${D} is R. The value of R is:",
                images = emptyList(),
                options = emptyList(),
                answer = "5",
                tolerance = 0.05,
                solution = "Center is (1, -2, 3). Radius ${D}R = \\sqrt{1 + 4 + 9 - (-11)} = \\sqrt{25} = 5${D}."
            )
        )

        val syllabus = mapOf(
            "Physics" to listOf(
                "Mechanics & Circular Motion",
                "Thermodynamics & Kinetic Theory of Gases",
                "Electromagnetic Theory & AC Circuits",
                "Ray Optics & Wave Optics",
                "Modern Physics, Atoms & Nuclei"
            ),
            "Chemistry" to listOf(
                "Chemical Bonding & Molecular Structure",
                "Chemical Kinetics & Equilibrium",
                "Organic Reaction Mechanisms & Stereochemistry",
                "Coordination Compounds & Transition Metals",
                "Ionic Equilibrium & Electrochemistry"
            ),
            "Maths" to listOf(
                "Integral Calculus & Differential Equations",
                "Matrices & Determinants",
                "Vectors & 3-Dimensional Geometry",
                "Limits, Continuity & Differentiability",
                "Coordinate Geometry & Conic Sections"
            )
        )

        return TestJson(
            schemaVersion = 1,
            title = "JEE Main 2026 (Online) 5th April Morning Slot",
            durationMin = 180,
            syllabus = syllabus,
            questions = questions
        )
    }

    fun getSampleTestJsonString(): String {
        return sampleJson.encodeToString(getSampleTest())
    }
}
