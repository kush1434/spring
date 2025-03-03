package com.nighthawk.spring_portfolio.mvc.seedtracker;

public class SeedTrackerRequest {
        private String name;
        private Double grade;
        public SeedTrackerRequest() {}
        public SeedTrackerRequest(String name, Double grade) {
            this.name = name;
            this.grade = grade;
        }
        public String getName() {
            return name;
        }
        public Double getGrade() {
            return grade;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setGrade(Double grade) {
            this.grade = grade;
        }
    }    

