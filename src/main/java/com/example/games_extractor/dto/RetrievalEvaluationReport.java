package com.example.games_extractor.dto;

import java.util.List;
//Ukupni report retrieval evaluacije za cijeli evaluation dataset
public class RetrievalEvaluationReport {

    private int totalExamples;

    private double hitRate;

    private double top1Accuracy;

    private double avgTopScore;

    private String collection;

    private List<Long> failures;

    private List<RetrievalEvaluationResult> perExample;

    public RetrievalEvaluationReport(
            int totalExamples,
            double hitRate,
            double top1Accuracy,
            double avgTopScore,
            String collection,
            List<Long> failures,
            List<RetrievalEvaluationResult> perExample) {

        this.totalExamples = totalExamples;
        this.hitRate = hitRate;
        this.top1Accuracy = top1Accuracy;
        this.avgTopScore = avgTopScore;
        this.collection = collection;
        this.failures = failures;
        this.perExample = perExample;
    }

    public int getTotalExamples() {
        return totalExamples;
    }

    public void setTotalExamples(int totalExamples) {
        this.totalExamples = totalExamples;
    }

    public double getHitRate() {
        return hitRate;
    }

    public void setHitRate(double hitRate) {
        this.hitRate = hitRate;
    }

    public double getTop1Accuracy() {
        return top1Accuracy;
    }

    public void setTop1Accuracy(double top1Accuracy) {
        this.top1Accuracy = top1Accuracy;
    }

    public double getAvgTopScore() {
        return avgTopScore;
    }

    public void setAvgTopScore(double avgTopScore) {
        this.avgTopScore = avgTopScore;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public List<Long> getFailures() {
        return failures;
    }

    public void setFailures(List<Long> failures) {
        this.failures = failures;
    }

    public List<RetrievalEvaluationResult> getPerExample() {
        return perExample;
    }

    public void setPerExample(List<RetrievalEvaluationResult> perExample) {
        this.perExample = perExample;
    }
}