package com.example.queuesystem;

public class Report {
    String DateTime,QueueSize;

    public Report(){}

    public Report(String dateTime, String queueSize) {
        DateTime = dateTime;
        QueueSize = queueSize;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public String getQueueSize() {
        return QueueSize;
    }

    public void setQueueSize(String queueSize) {
        QueueSize = queueSize;
    }
}
