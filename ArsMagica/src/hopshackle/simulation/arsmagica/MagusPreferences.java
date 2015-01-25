package hopshackle.simulation.arsmagica;

import java.util.*;

public class MagusPreferences {

	private Map<Learnable, Double> preferences = new HashMap<Learnable, Double>();

	public MagusPreferences() {
		new MagusPreferences(true);
	}

	public MagusPreferences(boolean uniform) {
		for (Abilities ability : Abilities.values()) {
			double val = 1.0;
			if (!uniform) {
				val =  generatePreference();
				if (ability == Abilities.MAGIC_THEORY || ability == Abilities.PARMA_MAGICA) {
					val = Math.max(val, generatePreference());
				}
			}
			if (ability == Abilities.VIS_HUNT || ability == Abilities.DECREPITUDE || ability == Abilities.WARPING)
				val = 0.0;
			preferences.put(ability, val);
		}
		for (Arts art : Arts.values()) {
			double val = 1.0;
			if (!uniform) {
				val =  generatePreference();
				val = Math.max(val, generatePreference());
			}
			preferences.put(art, val);
		}
		for (CovenantAttributes attribute : CovenantAttributes.values()) {
			double val = 1.0;
			if (!uniform)
				val = generatePreference();
			preferences.put(attribute, val);
		}
	}
	/*
	 * Generates a preference between 0 and 19 (roughly), with median at 0.866, and IQR of 0.298 to 2.381
	 */
	private double generatePreference() {
		return (1.0 / (1.05 - Math.random())) - (1.0/1.05);
	}
	public MagusPreferences(MagusPreferences base) {
		for (Learnable ability : Abilities.values()) {
			double target = base.getPreference(ability);
			double val = getNearestTo(target, 3);
			if (ability == Abilities.VIS_HUNT || ability == Abilities.DECREPITUDE || ability == Abilities.WARPING)
				val = 0.0;
			preferences.put(ability, val);
		}
		for (Learnable art : Arts.values()) {
			double target = base.getPreference(art);
			preferences.put(art, getNearestTo(target, 3));
		}
		for (CovenantAttributes attribute : CovenantAttributes.values()) {
			double target = base.getPreference(attribute);
			preferences.put(attribute, getNearestTo(target, 3));
		}
	}

	public void setPreference(Learnable target, double value) {
		preferences.put(target, value);
	}

	public double getPreference(Learnable option) {
		if (preferences.containsKey(option)) 
			return preferences.get(option);
		return 0.0;
	}

	public static Learnable getPreferenceGivenPriors(Magus magus, Map<Learnable, Double> options) {
		Learnable preference = null;
		double score = -1.0;
		for (Learnable option : options.keySet()) {
			if (getResearchPreference(magus, option) * options.get(option) > score) {
				score = getResearchPreference(magus, option) * options.get(option);
				preference = option;
			}
		}
		return preference;
	}
	private double getNearestTo(double target, int attempts) {
		double best = Double.MAX_VALUE / 2.0;
		for (int i = 0; i < attempts; i++) {
			double r = Math.random();
			if (Math.abs(r - target) < Math.abs(r - best)) {
				best = r;
			}
		}
		return best;
	}

	public static void setResearchPreference(Magus magus, Learnable target, double newValue) {
		MagusPreferences pref = magus.getResearchGoals();
		pref.setPreference(target, newValue);
	}
	public static double getResearchPreference(Magus magus, Learnable target) {
		MagusPreferences pref = magus.getResearchGoals();
		double level = magus.getLevelOf(target);
		return pref.getPreference(target) + level * 0.01 * (double)target.getMultiplier();
	}
}