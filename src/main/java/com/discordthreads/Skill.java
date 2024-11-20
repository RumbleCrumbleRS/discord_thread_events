package com.discordthreads;

import lombok.Getter;
import lombok.Setter;

public class Skill {

    @Setter
    @Getter
    private String skillName;

    @Setter
    @Getter
    private int skillLevel;

    public Skill(String skillName, int skillLevel) {
        this.skillName = skillName;
        this.skillLevel = skillLevel;
    }

    public void incrementSkillLevel() {
        this.skillLevel += 1;
    }

}
