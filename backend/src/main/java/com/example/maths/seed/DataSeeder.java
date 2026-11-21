package com.example.maths.seed;

import com.example.maths.model.Question;
import com.example.maths.repository.QuestionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private final QuestionRepository questionRepository;

    public DataSeeder(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (questionRepository.count() != 0) {
            return;
        }
        questionRepository.saveAll(List.of(
                // Algebra
                new Question("Algebra", "Solve 2x + 3 = 11. What is x?",
                        List.of("2", "4", "6", "8"), 1,
                        "Subtract 3 from both sides, then divide by 2.",
                        "2x = 8 so x = 4."),
                new Question("Algebra", "Solve 3x - 5 = 10. What is x?",
                        List.of("3", "4", "5", "6"), 2,
                        "Add 5 to both sides, then divide by 3.",
                        "3x = 15 so x = 5."),
                new Question("Algebra", "Expand (x + 2)(x + 3).",
                        List.of("x^2 + 6x + 5", "x^2 + 5x + 5", "x^2 + 6", "x^2 + 5x + 6"), 3,
                        "Multiply each term in the first bracket by each term in the second.",
                        "x*x + 3x + 2x + 6 = x^2 + 5x + 6."),
                new Question("Algebra", "Factorise x^2 - 9.",
                        List.of("(x - 3)(x + 3)", "(x - 9)(x + 1)", "(x - 3)^2", "(x + 3)^2"), 0,
                        "This is a difference of two squares: a^2 - b^2 = (a - b)(a + b).",
                        "x^2 - 9 = x^2 - 3^2 = (x - 3)(x + 3)."),
                // Fractions
                new Question("Fractions", "What is 1/2 + 1/3?",
                        List.of("1/5", "2/5", "5/6", "1/6"), 2,
                        "Use a common denominator of 6.",
                        "3/6 + 2/6 = 5/6."),
                new Question("Fractions", "What is 3/4 of 20?",
                        List.of("12", "15", "16", "18"), 1,
                        "Divide 20 by 4, then multiply by 3.",
                        "20 / 4 = 5 and 5 * 3 = 15."),
                new Question("Fractions", "What is 2/3 multiplied by 3/5?",
                        List.of("2/5", "1/3", "6/8", "3/8"), 0,
                        "Multiply the numerators and the denominators, then simplify.",
                        "(2 * 3) / (3 * 5) = 6/15 = 2/5."),
                new Question("Fractions", "Simplify 18/24.",
                        List.of("2/3", "9/16", "6/7", "3/4"), 3,
                        "Divide the numerator and denominator by their greatest common factor, 6.",
                        "18 / 6 = 3 and 24 / 6 = 4, so 18/24 = 3/4."),
                // Geometry
                new Question("Geometry", "What is the area of a rectangle measuring 6 by 4?",
                        List.of("10", "20", "24", "48"), 2,
                        "Area of a rectangle is length multiplied by width.",
                        "6 * 4 = 24."),
                new Question("Geometry", "What is the sum of the interior angles of a triangle, in degrees?",
                        List.of("180", "90", "360", "270"), 0,
                        "Think about the angles of a triangle added together.",
                        "The interior angles of any triangle always sum to 180 degrees."),
                new Question("Geometry", "What is the circumference of a circle with radius 7? Give your answer in terms of pi.",
                        List.of("7\u03C0", "49\u03C0", "14\u03C0", "21\u03C0"), 2,
                        "Circumference is 2 times pi times the radius.",
                        "C = 2 * pi * 7 = 14\u03C0."),
                new Question("Geometry", "A right-angled triangle has shorter sides 3 and 4. What is the hypotenuse?",
                        List.of("6", "5", "7", "12"), 1,
                        "Use Pythagoras: a^2 + b^2 = c^2.",
                        "3^2 + 4^2 = 9 + 16 = 25, so c = 5."),
                // Percentages
                new Question("Percentages", "What is 20% of 150?",
                        List.of("15", "25", "30", "35"), 2,
                        "Convert 20% to 0.2 and multiply.",
                        "0.2 * 150 = 30."),
                new Question("Percentages", "What is 50 increased by 10%?",
                        List.of("55", "45", "60", "50"), 0,
                        "Find 10% of 50 and add it to 50.",
                        "10% of 50 is 5, and 50 + 5 = 55."),
                new Question("Percentages", "40 is what percentage of 80?",
                        List.of("25%", "40%", "50%", "200%"), 2,
                        "Divide the part by the whole and multiply by 100.",
                        "40 / 80 = 0.5, which is 50%."),
                new Question("Percentages", "A price of 200 is reduced by 25%. What is the new price?",
                        List.of("175", "125", "160", "150"), 3,
                        "Find 25% of 200 and subtract it.",
                        "25% of 200 is 50, and 200 - 50 = 150."),
                // Probability
                new Question("Probability", "A fair six-sided die is rolled. What is the probability of an even number?",
                        List.of("1/6", "1/3", "1/2", "2/3"), 2,
                        "Count the even faces out of six equally likely faces.",
                        "3 of the 6 faces (2, 4, 6) are even, so the probability is 3/6 = 1/2."),
                new Question("Probability", "Two fair coins are tossed. What is the probability both show heads?",
                        List.of("1/4", "1/2", "1/8", "1/3"), 0,
                        "List the outcomes: HH, HT, TH, TT.",
                        "Only 1 of the 4 equally likely outcomes is HH, so the probability is 1/4."),
                new Question("Probability", "One card is drawn from a standard 52-card deck. What is the probability it is red?",
                        List.of("1/4", "1/2", "1/13", "3/4"), 1,
                        "A standard deck has 26 red cards and 26 black cards.",
                        "26 out of 52 cards are red, so the probability is 1/2."),
                new Question("Probability", "An event has probability 0.3 of occurring. What is the probability it does not occur?",
                        List.of("0.3", "0.5", "0.6", "0.7"), 3,
                        "The probabilities of an event and its complement add up to 1.",
                        "1 - 0.3 = 0.7.")
        ));
    }
}
