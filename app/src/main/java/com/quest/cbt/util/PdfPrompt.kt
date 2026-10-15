package com.quest.cbt.util

/**
 * Prompt provided to AI models (Claude, ChatGPT, Gemini) to convert raw
 * question paper PDFs and answer keys into the Quest CBT JSON schema.
 */
const val PDF_TO_JSON_PROMPT = """You are an expert JEE Main question paper parser.
Convert the attached question paper and answer key into the exact Quest CBT JSON schema version 1.

OUTPUT RULES:
1. Output valid JSON matching schemaVersion 1.
2. If images are present in the PDF (diagrams, graphs, circuits, structural formulas):
   - Crop and save each image into an "images/" folder as PNG or JPG (e.g. images/q1_1.png).
   - In the JSON, reference the relative path in the question's "images" or "options[i].image" array.
   - Bundle test.json and the images/ folder together in a single ZIP file.
3. Every math formula, variable, fraction, square root, exponent, or symbol MUST be wrapped in LaTeX delimiters ($...$ for inline or $$...$$ for block).
   Example: $\frac{1}{2}mv^2$, $\Delta G = \Delta H - T\Delta S$, $\int_0^\pi \sin(x) dx$.
4. Ensure Physics, Chemistry, and Maths each have their respective questions (Section A: MCQ with exactly 4 options A, B, C, D; Section B: NUM with numeric answer).
5. For BONUS questions, set "answer": "BONUS".
6. For NUM questions, include a reasonable "tolerance" (e.g., 0.01 or 0.05) if rounding applies.

JSON SCHEMA:
{
  "schemaVersion": 1,
  "title": "<Exam Title / Shift Name>",
  "durationMin": 180,
  "syllabus": {
    "Physics": ["<Chapter Names>"],
    "Chemistry": ["<Chapter Names>"],
    "Maths": ["<Chapter Names>"]
  },
  "questions": [
    {
      "id": 1,
      "subject": "Physics",
      "section": "A",
      "type": "MCQ",
      "text": "Question statement with LaTeX...",
      "images": [],
      "options": [
        {"text": "Option A...", "image": null},
        {"text": "Option B...", "image": null},
        {"text": "Option C...", "image": null},
        {"text": "Option D...", "image": null}
      ],
      "answer": "A",
      "tolerance": 0.01,
      "solution": "Step-by-step solution with LaTeX...",
      "solutionImages": []
    }
  ]
}
"""
