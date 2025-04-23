package tn.esprit.new_timetableservice.dto;

public class ConstraintCheck {
    private int required;
    private int available;
    private boolean satisfied;

    public ConstraintCheck(int required, int available, boolean satisfied) {
        this.required = required;
        this.available = available;
        this.satisfied = satisfied;
    }

    // Getters and setters
    public int getRequired() { return required; }
    public void setRequired(int required) { this.required = required; }
    public int getAvailable() { return available; }
    public void setAvailable(int available) { this.available = available; }
    public boolean isSatisfied() { return satisfied; }
    public void setSatisfied(boolean satisfied) { this.satisfied = satisfied; }
}