
package jp.ac.tokushima_u.is.ll.sphinx.classes;

import java.util.Date;

public class Quiz {

    // LLO オブジェクトID
    private String[] quizId;
    
    // Quizの名前
    private String[] name;

    // Quiz製作者
    private String author;

    // Quizを作った/受信した日時(Date)
    private Date createdAt;
    
    // クイズの答え
    private int answer;

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public String[] getQuizId() {
        return quizId;
    }

    public void setQuizId(String[] quizId) {
        this.quizId = quizId;
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
