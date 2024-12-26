package uk.co.scottdennison.java.libs.datastructure;

import java.util.Objects;

public final class Pair<LeftType,RightType> {
    private final LeftType left;
    private final RightType right;

    public Pair(LeftType left, RightType right) {
        this.left = left;
        this.right = right;
    }

    public LeftType getLeft() {
        return left;
    }

    public RightType getRight() {
        return right;
    }

    @Override
    public final boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (!(otherObject instanceof Pair)) return false;

        Pair<?, ?> otherPair = (Pair<?, ?>) otherObject;
        return Objects.equals(left, otherPair.left) && Objects.equals(right, otherPair.right);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(left);
        result = 31 * result + Objects.hashCode(right);
        return result;
    }

    @Override
    public String toString() {
        return "Pair{" +
            "left=" + left +
            ", right=" + right +
            '}';
    }
}
