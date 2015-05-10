package jp.ac.tokushima_u.is.ll.sphinx.classes;

import org.msgpack.annotation.Message;

@Message
public class MessagedQuiz {
    public String[] imageId;
    public String[] name;
    public int answer;
    public String author;
}
