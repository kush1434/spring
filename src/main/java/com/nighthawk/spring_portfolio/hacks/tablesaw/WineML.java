package com.nighthawk.spring_portfolio.hacks.tablesaw;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import tech.tablesaw.api.*;
import tech.tablesaw.columns.Column;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.Histogram;
import tech.tablesaw.plotly.api.ScatterPlot;
import tech.tablesaw.plotly.components.Figure;

import smile.classification.*;
import smile.data.*;
import smile.data.formula.Formula;
import smile.data.vector.DoubleVector;
import smile.data.vector.IntVector;
import smile.math.MathEx;
import smile.validation.metric.Accuracy;

import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class WineML {
    /**
     * This program is based on the Wine lesson notebook, this is in one file to avoid dependency issues in the notebook.
     */
    public static void main(String[] args) {
        Table wine = Table.read().csv(System.getProperty("user.home") + "/wine-dataset/WineQT.csv");

        System.out.println("Dataset structure:");
        System.out.println(wine.structure());
        System.out.println("\nDataset shape:");
        System.out.println(wine.shape());
        System.out.println("\nFirst 5 rows:");
        System.out.println(wine.first(5));
        System.out.println("\nSummary:");
        System.out.println(wine.summary());

        waitUntilEnter();


        // Remove the second parameter if you want it to show directly rather than to a file, it doesn't work in WSL
        Figure hist = Histogram.create("Alcohol", wine.numberColumn("alcohol"));
        Figure scatter = ScatterPlot.create("Alcohol vs Quality", wine, "alcohol", "quality");

        // uncomment to show directly in a GUI, doesn't work in WSL
        // Plot.show(hist);
        // Plot.show(scatter);

        writePlotToFile(hist, "alcohol_histogram.html");
        writePlotToFile(scatter, "alcohol_vs_quality.html");

        waitUntilEnter();


        // convert the tablesaw table to a Smile DataFrame
        String[] colNames = wine.columnNames().toArray(String[]::new);
        double[][] data = wine.as().doubleMatrix();
        DataFrame df = DataFrame.of(data, colNames);

        IntVector quality = IntVector.of("quality", df.doubleVector("quality").stream()
            .mapToInt(d -> (int) d)
            .toArray());
        df = df.drop("quality").merge(quality);


        // here we just split data into training and test (80% train, 20% test)
        int n = df.nrows();
        int[] indices = IntStream.range(0, n).toArray();
        MathEx.permutate(indices); 

        int splitIndex = (int)(n * 0.75);
        System.out.println(splitIndex + " rows for training, " + (n - splitIndex) + " rows for testing.");

        DataFrame trainDf = df.slice(0, splitIndex);
        DataFrame testDf = df.slice(splitIndex, n);
        RandomForest rf = RandomForest.fit(Formula.lhs("quality"), trainDf);

        waitUntilEnter();


        int[] yTrue = testDf.stream().mapToInt(r -> r.getInt("quality")).toArray();
        int[] yPred = testDf.drop("quality")
                          .stream()
                          .mapToInt(rf::predict)
                          .toArray();


        double accuracy = Accuracy.of(yTrue, yPred);
        System.out.printf("Model Accuracy: %.2f%%%n", accuracy * 100);

    }

    private static void writePlotToFile(Figure figure, String filename) {
        try (FileWriter writer = new FileWriter(Paths.get(filename).toFile())) {
            writer.write("<html><head><script src=\"https://cdn.plot.ly/plotly-3.0.1.min.js\" charset=\"utf-8\"></script><title>Plot</title></head><body><div id=\"myPlot\"></div>");
            writer.write(figure.asJavascript("myPlot"));
            writer.write("</body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void waitUntilEnter() {
        System.out.println("Press Enter to continue...");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
        } catch (Exception e) {}
    }
}
