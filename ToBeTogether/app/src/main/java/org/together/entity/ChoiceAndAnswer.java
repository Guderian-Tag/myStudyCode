package org.together.entity;

/**
 * Created by v-fei.wang on 2015/12/7.
 */
public class ChoiceAndAnswer {
    private Integer choiceId;
    private Integer answerLabelId;
    private int answerLabelIndex;
    private String answerLabelText;

    public Integer getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(Integer choiceId) {
        this.choiceId = choiceId;
    }

    public Integer getAnswerLabelId() {
        return answerLabelId;
    }

    public void setAnswerLabelId(Integer answerLabelId) {
        this.answerLabelId = answerLabelId;
    }

    public int getAnswerLabelIndex() {
        return answerLabelIndex;
    }

    public void setAnswerLabelIndex(int answerLabelIndex) {
        this.answerLabelIndex = answerLabelIndex;
    }

    public String getAnswerLabelText() {
        return answerLabelText;
    }

    public void setAnswerLabelText(String answerLabelText) {
        this.answerLabelText = answerLabelText;
    }
}
