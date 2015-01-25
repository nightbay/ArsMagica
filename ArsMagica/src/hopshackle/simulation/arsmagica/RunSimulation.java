package hopshackle.simulation.arsmagica;

import java.io.File;
import java.util.*;

import hopshackle.simulation.*;

public class RunSimulation {

	private static final World world = new World();
	private static Covenant sampleCovenant = new Covenant(null, null);
	private static Tribunal rhine;
	private static Tribunal rome;
	private static Tribunal alps;
	private static Tribunal normandy;
	private static Tribunal iberia;
	private static Tribunal provencal;
	private static Tribunal stonehenge;
	private static Tribunal transylvania;
	private static Tribunal thebes;
	private static Tribunal hibernia;
	private static Tribunal lochLeglean;
	private static Tribunal novgorod;
	private static Tribunal levant;
	private static String baseDir = SimProperties.getProperty("BaseDirectory", "C:\\");
	private static List<Tribunal> tribunals;

	public static void main(String[] args) {
		int startYear = 700;
		int yearsToRun = 520;
		FastCalendar cal = new FastCalendar(startYear * 52);
		world.setCalendar(cal, 52);
		ActionProcessor ap = new ActionProcessor("ARS_TEST_01", false);
		ap.setWorld(world);
		world.setActionProcessor(ap);
		world.setName("AM3");
		rhine = new Tribunal("Rhine", world);
		rome = new Tribunal("Rome", world);
		normandy = new Tribunal("Normandy", world);
		transylvania = new Tribunal("Transylvania", world);
		alps = new Tribunal("Greater Alps", world);
		stonehenge = new Tribunal("Stonehenge", world);
		provencal = new Tribunal("Provencal", world);
		iberia = new Tribunal("Iberia", world);
		tribunals = new ArrayList<Tribunal>();
		tribunals.add(rhine);
		tribunals.add(rome);
		tribunals.add(alps);
		tribunals.add(normandy);
		tribunals.add(iberia);
		tribunals.add(provencal);
		tribunals.add(stonehenge);
		tribunals.add(transylvania);

		DatabaseAccessUtility dbu = new DatabaseAccessUtility();
		Thread t = new Thread(dbu);
		t.start();
		world.setDatabaseAccessUtility(dbu);

		List<String> startingMagi = HopshackleUtilities.createListFromFile(new File(baseDir + "\\StartingMagi.txt"));
		for (int i = 1; i < startingMagi.size(); i++) {
			// first line is field headers
			String line = startingMagi.get(i);
			String[] fields = line.split("\t");
			Magus founder = new Magus(world);
			founder.setAge(30);
			founder.setName(fields[0]);
			for (HermeticHouse house : HermeticHouse.values())  {
				String houseName = house.toString().toLowerCase();
				houseName = houseName.substring(0, 1).toUpperCase() + houseName.substring(1);
				if (houseName.equals(fields[0]))
					founder.setHermeticHouse(house);
			}
			founder.setIntelligence(Integer.valueOf(fields[16]));
			founder.setPerception(Integer.valueOf(fields[17]));
			founder.setStrength(Integer.valueOf(fields[18]));
			founder.setStamina(Integer.valueOf(fields[19]));
			founder.setPresence(Integer.valueOf(fields[20]));
			founder.setCommunication(Integer.valueOf(fields[21]));
			founder.setDexterity(Integer.valueOf(fields[22]));
			founder.setQuickness(Integer.valueOf(fields[23]));
			founder.addXP(Abilities.LATIN, 75);
			founder.addXP(Abilities.ARTES_LIBERALES, 30);
			founder.addXP(Abilities.PARMA_MAGICA, 15);
			founder.addXP(Abilities.MAGIC_THEORY, 15);
			for (int j = 0; j < 15; j++) {
				founder.addXP(Arts.random(), 25);
			}
			for (int artIndex = 1; artIndex <= 15; artIndex++) {
				if (fields[artIndex].isEmpty())
					continue;
				Arts a = null;
				switch (artIndex) {
				case  1: a = Arts.CREO; break;
				case  2: a = Arts.INTELLEGO; break;
				case  3: a = Arts.MUTO; break;
				case  4: a = Arts.PERDO; break;
				case  5: a = Arts.REGO; break;
				case  6: a = Arts.ANIMAL; break;
				case  7: a = Arts.AQUAM; break;
				case  8: a = Arts.AURAM; break;
				case  9: a = Arts.CORPUS; break;
				case 10: a = Arts.HERBAM; break;
				case 11: a = Arts.IGNEM; break;
				case 12: a = Arts.IMAGINEM; break;
				case 13: a = Arts.MENTEM; break;
				case 14: a = Arts.TERRAM; break;
				case 15: a = Arts.VIM; break;
				}
				int xp = Integer.valueOf(fields[artIndex]);
				founder.addXP(a, xp);
				double currentPref = MagusPreferences.getResearchPreference(founder, a);
				currentPref = Math.max(currentPref, (double)xp/50.0);
				MagusPreferences.setResearchPreference(founder, a, currentPref);
			}

			for (Abilities ability : Abilities.values()) 
				if (ability.toString().equals(fields[24])) {
					double currentPref = MagusPreferences.getResearchPreference(founder, ability);
					founder.addXP(ability, 75);
					MagusPreferences.setResearchPreference(founder, ability, Math.max(3.0, currentPref));
				}
			for (Tribunal tr : tribunals) 
				if (tr.toString().equals(fields[25]))
					founder.setTribunal(tr);

			world.addAction(founder.decide());
		}
		ap.start();



		rhine.addAccessibleLocation(normandy);
		normandy.addAccessibleLocation(rhine);
		rhine.addAccessibleLocation(transylvania);
		transylvania.addAccessibleLocation(rhine);
		rhine.addAccessibleLocation(alps);
		alps.addAccessibleLocation(rhine);
		rhine.addAccessibleLocation(stonehenge);
		stonehenge.addAccessibleLocation(rhine);

		rome.addAccessibleLocation(alps);
		alps.addAccessibleLocation(rome);
		rome.addAccessibleLocation(transylvania);
		transylvania.addAccessibleLocation(rome);
		rome.addAccessibleLocation(alps);
		alps.addAccessibleLocation(rome);
		rome.addAccessibleLocation(provencal);
		provencal.addAccessibleLocation(rome);
		rome.addAccessibleLocation(iberia);
		iberia.addAccessibleLocation(rome);

		normandy.addAccessibleLocation(alps);
		alps.addAccessibleLocation(normandy);
		normandy.addAccessibleLocation(stonehenge);
		stonehenge.addAccessibleLocation(normandy);
		normandy.addAccessibleLocation(provencal);
		provencal.addAccessibleLocation(normandy);
		normandy.addAccessibleLocation(iberia);
		iberia.addAccessibleLocation(normandy);

		alps.addAccessibleLocation(provencal);
		provencal.addAccessibleLocation(alps);

		provencal.addAccessibleLocation(iberia);
		iberia.addAccessibleLocation(provencal);

		world.setScheduledTask(new TimerTask() {

			@Override
			public void run() {
				List<Agent> allAgents = world.getAgents();
				HashMap<HermeticHouse, Integer> houseMembership = new HashMap<HermeticHouse, Integer>();
				for (HermeticHouse h : HermeticHouse.values())
					houseMembership.put(h, 0);
				int totalCovenants = world.getAllChildLocationsOfType(sampleCovenant).size();
				int magi = 0, apprentices = 0;
				for (Agent a : allAgents) {
					if (a instanceof Magus) {
						Magus m = (Magus) a;
						houseMembership.put(m.getHermeticHouse(), houseMembership.get(m.getHermeticHouse())+1);
						if (m.isApprentice())
							apprentices++;
						else
							magi++;
					}
				}
				for (HermeticHouse h : HermeticHouse.values()) 
					h.updateApprenticeshipModifier(houseMembership.get(h), apprentices+magi);
			
				System.out.println(String.format("Year %d:		%d Magi, %d apprentices in %d covenants.", world.getYear(), magi, apprentices, totalCovenants));
				
			}
		}, 260, 260);


		world.setScheduledTask(new TimerTask() {

			@Override
			public void run() {
				int allAgents = world.getAgents().size();
				int totalCovenants = world.getAllChildLocationsOfType(sampleCovenant).size();
				int magi = allAgents - totalCovenants;
				if (magi < (world.getYear() - 500) / 2) {
					Magus hedgeWizard = new Magus(world);
					List<Agent> all = world.getAgents();
					HashMap<HermeticHouse, Integer> allHouses = new HashMap<HermeticHouse, Integer>();
					for (HermeticHouse h : HermeticHouse.values())
						allHouses.put(h, 0);
					for (int i = 0; i < 50; i++) {
						Agent potential = all.get(Dice.roll(1, all.size())-1);
						if (potential instanceof Magus) {
							HermeticHouse h = ((Magus)potential).getHermeticHouse();
							if (h != null)
								allHouses.put(h,allHouses.get(h)+1);
						}
					} 
					HermeticHouse sponsor = null;
					int lowestCount = 50;
					for (HermeticHouse h : HermeticHouse.values()) {
						int thisHouse = allHouses.get(h);
						if (h == HermeticHouse.BONISAGUS || h == HermeticHouse.MERCERE || h == HermeticHouse.TRIANOMA)
							thisHouse = thisHouse * 2;
						if (thisHouse < lowestCount) {
							lowestCount = thisHouse;
							sponsor = h;
						}
					}
					hedgeWizard.setAge(30);
					hedgeWizard.addXP(Abilities.LATIN, 50);
					hedgeWizard.addXP(Abilities.ARTES_LIBERALES, 30);
					hedgeWizard.addXP(Abilities.PARMA_MAGICA, 5);
					hedgeWizard.addXP(Abilities.MAGIC_THEORY, 5);
					hedgeWizard.log("Is inducted into the Order of Hermes by " + sponsor);
					hedgeWizard.setHermeticHouse(sponsor);
					for (int j = 0; j < 3; j++) {
						hedgeWizard.addXP(Arts.random(), 50);
					}
					for (int j = 0; j < 7; j++) {
						hedgeWizard.addXP(Arts.random(), 20);
					}
					world.addAction(hedgeWizard.decide());
				}

			}
		}, 260, 260);


		world.setScheduledTask(new TimerTask() {

			@Override
			public void run() {
				world.worldDeath();	
			}
		}, yearsToRun * 52);



		world.setScheduledTask(new TimerTask() {

			@Override
			public void run() {
				thebes = new Tribunal("Thebes", world);
				hibernia = new Tribunal("Hibernia", world);
				lochLeglean = new Tribunal("Loch Leglean", world);

				thebes.addAccessibleLocation(transylvania);
				transylvania.addAccessibleLocation(thebes);
				thebes.addAccessibleLocation(rome);
				rome.addAccessibleLocation(thebes);

				hibernia.addAccessibleLocation(stonehenge);
				stonehenge.addAccessibleLocation(hibernia);
				hibernia.addAccessibleLocation(lochLeglean);
				lochLeglean.addAccessibleLocation(hibernia);

				lochLeglean.addAccessibleLocation(stonehenge);
				stonehenge.addAccessibleLocation(lochLeglean);
			}
		}, 200 * 52);

		world.setScheduledTask(new TimerTask() {

			@Override
			public void run() {
				levant = new Tribunal("Levant", world);
				novgorod = new Tribunal("Novgorod", world);

				levant.addAccessibleLocation(thebes);
				thebes.addAccessibleLocation(levant);
				levant.addAccessibleLocation(rome);
				rome.addAccessibleLocation(levant);

				novgorod.addAccessibleLocation(transylvania);
				transylvania.addAccessibleLocation(novgorod);
				novgorod.addAccessibleLocation(thebes);
				thebes.addAccessibleLocation(novgorod);
			}
		}, 350 * 52);
	}
}