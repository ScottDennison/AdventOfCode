package uk.co.scottdennison.java.soft.challenges.adventofcode.puzzles.year2015;

import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.BasicPuzzleResults;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzle;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleConfigProvider;
import uk.co.scottdennison.java.soft.challenges.adventofcode.framework.IPuzzleResults;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day21 implements IPuzzle {
    private static final Pattern PATTERN = Pattern.compile("\\s*Hit Points:\\s*(?<hitPoints>[0-9]+)\\s*Damage:\\s*(?<damageRating>[0-9]+)\\s*Armor:\\s*(?<armorRating>[0-9]+)\\s*");

    private static class Item {
        private final String name;
        private final int cost;
        private final int damageRating;
        private final int armorRating;

        private Item(String name, int cost, int damageRating, int armorRating) {
            this.name = name;
            this.cost = cost;
            this.damageRating = damageRating;
            this.armorRating = armorRating;
        }

        public String getName() {
            return this.name;
        }

        public int getCost() {
            return this.cost;
        }

        public int getDamageRating() {
            return this.damageRating;
        }

        public int getArmorRating() {
            return this.armorRating;
        }
    }

    private static class FightParticipant {
        private final int hitPoints;
        private final int damageRating;
        private final int armorRating;

        private FightParticipant(int hitPoints, int damageRating, int armorRating) {
            this.hitPoints = hitPoints;
            this.damageRating = damageRating;
            this.armorRating = armorRating;
        }

        public int getHitsUntilDeath(FightParticipant otherFightParticipant) {
            int lossPerHit = Math.max(1,otherFightParticipant.getDamageRating()-armorRating);
            int hitsUntilDeath = hitPoints / lossPerHit;
            if ((hitsUntilDeath*lossPerHit) < hitPoints) {
                hitsUntilDeath++;
            }
            return hitsUntilDeath;
        }

        public boolean canWinFight(FightParticipant otherFightParticipant, boolean goFirst) {
            return (getHitsUntilDeath(otherFightParticipant)-otherFightParticipant.getHitsUntilDeath(this)) >= (goFirst?0:1);
        }

        public int gethitPoints() {
            return this.hitPoints;
        }

        public int getDamageRating() {
            return this.damageRating;
        }

        public int getArmorRating() {
            return this.armorRating;
        }
    }

    private static class Player extends FightParticipant {
        private final int coinsSpent;

        public Player(FightParticipant fightParticipant) {
            this(fightParticipant.gethitPoints(), fightParticipant.getDamageRating(), fightParticipant.getArmorRating(), 0);
        }

        public Player(int hitPoints, int damageRating, int armorRating, int coinsSpent) {
            super(hitPoints, damageRating, armorRating);
            this.coinsSpent = coinsSpent;
        }

        public Player equip(Item item) {
            return new Player(
                gethitPoints(),
                getDamageRating()+item.getDamageRating(),
                getArmorRating()+item.getArmorRating(),
                coinsSpent + item.getCost()
            );
        }

        public int getCoinsSpent() {
            return this.coinsSpent;
        }
    }

    private static final Item DUMMY_ITEM = new Item(null,0,0,0);

    private static final Item[] WEAPONS = {
        new Item("Dagger",8,4,0),
        new Item("Shortsword",10,5,0),
        new Item("Warhammer",25,6,0),
        new Item("Longsword",40,7,0),
        new Item("Greataxe",74,8,0)
    };

    private static final Item[] ARMOR = {
        DUMMY_ITEM,
        new Item("Leather",13,0,1),
        new Item("Chainmail",31,0,2),
        new Item("Splintmail",53,0,3),
        new Item("Bandedmail",75,0,4),
        new Item("Platemail",102,0,5)
    };

    private static final Item[] RINGS = {
        DUMMY_ITEM,
        DUMMY_ITEM,
        new Item("Damage +1",25,1,0),
        new Item("Damage +2",50,2,0),
        new Item("Damage +3",100,3,0),
        new Item("Defense +1",20,0,1),
        new Item("Defense +2",40,0,2),
        new Item("Defense +3",80,0,3)
    };

    @Override
    public IPuzzleResults runPuzzle(char[] inputCharacters, IPuzzleConfigProvider configProvider, boolean partBPotentiallyUnsolvable, PrintWriter printWriter) {
        Player basePlayer = new Player(parseFightParticipant(configProvider.getPuzzleConfigChars("player")));
        FightParticipant boss = parseFightParticipant(inputCharacters);
        int weaponCount = WEAPONS.length;
        int armorCount = ARMOR.length;
        int ringCount = RINGS.length;
        int lowestSurvivingCost = Integer.MAX_VALUE;
        int highestDyingCost = Integer.MIN_VALUE;
        for (int weaponIndex=0; weaponIndex<weaponCount; weaponIndex++) {
            Player playerAfterWeapon = basePlayer.equip(WEAPONS[weaponIndex]);
            for (int armorIndex=0; armorIndex<armorCount; armorIndex++) {
                Player playerAfterArmor = playerAfterWeapon.equip(ARMOR[armorIndex]);
                for (int ring1Index=0; ring1Index<ringCount; ring1Index++) {
                    Player playerAfterRing1 = playerAfterArmor.equip(RINGS[ring1Index]);
                    for (int ring2Index=ring1Index+1; ring2Index<ringCount; ring2Index++) {
                        Player playerAfterRing2 = playerAfterRing1.equip(RINGS[ring2Index]);
                        int cost = playerAfterRing2.getCoinsSpent();
                        if (playerAfterRing2.canWinFight(boss,true)) {
                            if (cost < lowestSurvivingCost) {
                                lowestSurvivingCost = cost;
                            }
                        }
                        else {
                            if (cost > highestDyingCost) {
                                highestDyingCost = cost;
                            }
                        }
                    }
                }
            }
        }
        return new BasicPuzzleResults<>(
            lowestSurvivingCost,
            highestDyingCost
        );
    }

    private FightParticipant parseFightParticipant(char[] chars) {
        Matcher matcher = PATTERN.matcher(new String(chars));
        if (!matcher.matches()) {
            throw new IllegalStateException("Could not parse fight participant string");
        }
        return new FightParticipant(
            Integer.parseInt(matcher.group("hitPoints")),
            Integer.parseInt(matcher.group("damageRating")),
            Integer.parseInt(matcher.group("armorRating"))
        );
    }
}
