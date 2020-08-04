package xyz.mcfridays.base.score;

public abstract class ScoreData implements Comparable<ScoreData> {
	private int score;

	public ScoreData(int score) {
		this.score = score;
	}

	public int getScore() {
		return score;
	}

	@Override
	public int compareTo(ScoreData o) {
		return o.getScore() - this.getScore();
	}

	@Override
	public String toString() {
		return getScore() + "";
	}
}