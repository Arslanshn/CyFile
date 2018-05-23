package at.tugraz.tc.cyfile.secret.impl;

import com.andrognito.patternlockview.PatternLockView;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import at.tugraz.tc.cyfile.secret.Secret;

public class PinPatternSecret implements Secret {

    private List<PatternLockView.Dot> dotList;

    private String patterns;

    public PinPatternSecret(List<PatternLockView.Dot> pattern) {
        this.dotList = pattern;
    }

    public PinPatternSecret(String patterns) {
        this.patterns = patterns;
    }

    @Override
    public String getSecretValue() {
        if (patterns != null) {
            return patterns;
        }

        if (dotList != null) {
            StringBuilder str = new StringBuilder();

            for (PatternLockView.Dot dot :
                    dotList) {
                str.append(dot.getRow()).append(dot.getColumn());
            }
            return str.toString();
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Secret that = (Secret) o;

        return new EqualsBuilder()
                .append(this.getSecretValue(), that.getSecretValue())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(this.getSecretValue())
                .toHashCode();
    }
}
