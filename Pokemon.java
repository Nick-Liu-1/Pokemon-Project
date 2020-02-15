/*
Pokemon.java
Nick Liu
ICS4U-01
Class that deals with the Pokemon objects.
 */

import java.util.*;

public class Pokemon {
    // Stats of Pokemon
    private final String name;
    private int hp;  // Current HP
    private final int maxHP;  // Max HP attainable by Pokemon
    private int energy = 50;
    private final String type;
    private final String resistance;
    private final String weakness;
    private final ArrayList<Attack> attacks = new ArrayList<>();  // Arraylist of attacks that can be used
    private boolean stunned;  // If Pokemon stunned or not
    private boolean disabled;  // If Pokemon is disabled or not


    public class Attack {
        // Stats of Attack
        private String name;
        private int cost;
        private int damage;
        private String special;

        // Attack constructor
        public Attack(String name, int cost, int damage, String special) {
            this.name = name;
            this.cost = cost;
            this.damage = damage;
            if (special.equals("")) {
                this.special = "NONE";  // If there is no special its special is "NONE"
            }
            else {
                this.special = special;
            }

        }

        // Getter methods
        public int getDamage() {
            return damage;
        }

        public String getName() {
            return name;
        }

        public int getCost() { return cost; }

        public String getSpecial() { return special; }

        public String toString() {
            return name + " " + cost + " " + damage + " " + special;
        }
    }

    // Pokemon constructor
    public Pokemon(String name, int hp, String type, String resistance, String weakness, ArrayList<ArrayList<String>> attacks) {
        // Setting all of the stats
        this.name = name;
        this.hp = hp;
        maxHP = hp;
        this.type = type;
        this.resistance = resistance;
        this.weakness = weakness;
        for (ArrayList<String> attack : attacks) {  // Converting the arraylists of stats into Attack objects
            this.attacks.add(new Attack(attack.get(0), Integer.parseInt(attack.get(1)), Integer.parseInt(attack.get(2)), attack.get(3).trim()));
        }

    }

    // Getter and setter methods
    public String getName() { return name; }

    public int getHp() {
        return hp;
    }

    public int getEnergy() { return energy; }

    public String getType() { return type; }

    public ArrayList<Attack> getAttacks() { return attacks; }

    public boolean getStunned() { return stunned; }

    // Set disable
    public void unDisable() {
        disabled = false;
    }

    // Set stun
    public void unStun() {
        stunned = false;
    }

    public boolean attack(Attack a, Pokemon defendingPokemon) {
        /*
            Takes in the specified attack and defending Pokemon and deals appropriate damage. Resolves
            weakness, resistance, and special attacks. Returns true if attack was successful otherwise
            false.
         */

        // Subtract the energy. If there is not enough energy return false as attack was unsuccessful
        if (energy - a.cost >= 0) {
            energy -= a.cost;
        }
        else {
            return false;
        }

        System.out.printf("%s used %s\n", name, a.name);
        int damage = calcDamage(a, defendingPokemon);  // Damage of the attack after applying weakness/resistance

        boolean attackHit = true;  // For attacks that are not guaranteed to hit, tracks if the attack hit or not
        boolean specialSuccess = Main.randint(0, 1) == 1;  // If the random chance specials are successful or not

        // Resolving specials
        switch(a.special) {
            case "stun":
                // If successful, stun the defending Pokemon
                if (specialSuccess) {
                    defendingPokemon.stunned = true;
                    System.out.println(defendingPokemon.name + " has been stunned!");
                }
                break;
            case "wild card":
                // If "successful," reduce the damage to 0 and set attackHit to false
                if (specialSuccess) {
                    damage = 0;
                    System.out.printf("%s's attack did nothing!\n", name);
                    attackHit = false;
                }
                break;
            case "wild storm":
                boolean firstIteration = true; // If the hit was the first iteration or not
                // Unsuccessful case, print that nothing happened
                if (!specialSuccess) {
                    System.out.println(name + "'s attack did nothing!");
                    attackHit = false;
                }

                // Successful case, keep rolling as long as you are successful
                while (specialSuccess) {
                    if (!firstIteration) {  // Print that additional damage was dealt if it is not first iteration
                        System.out.printf("%s dealt additional damage with wild storm!\n", name);
                    }
                    defendingPokemon.hp = Math.max(defendingPokemon.hp - damage, 0);   // Dealing damage
                    specialSuccess = Main.randint(0, 1) == 1;  // Reroll to see if it is successful again
                    firstIteration = false;
                }
                damage = 0;  // Set damage to 0 so no additional damaged is dealt during the common damage dealing for all cases
                break;
            case "disable":
                // Disable the defending Pokemon
                defendingPokemon.disabled = true;
                System.out.printf("%s has been disabled!\n", defendingPokemon.name);
                break;
            case "recharge":
                // Restore the energy to the attacking Pokemon
                energy = Math.min(energy + 20, 50);
                System.out.printf("%s has recharged 20 ENERGY!\n", name);
                break;
        }

        // Weakness/resistance effect messages; only display if attack actually hits
        if (defendingPokemon.resistance.equals(type) && attackHit) {
            System.out.println("It's not very effective...");
        }
        if (defendingPokemon.weakness.equals(type) && attackHit) {
            System.out.println("It's super effective!");
        }

        // Dealing the damage by modifying hp
        defendingPokemon.hp = Math.max(defendingPokemon.hp - damage, 0);

        // Return that attack was successful
        return true;
    }

    public int calcDamage(Attack a, Pokemon defendingPokemon) {
        /*
            Applies disabled, weakness, and resistance to the damage value and returns it.
         */

        int damage = a.damage;
        if (disabled) {
            damage = Math.max(damage - 10, 0);
        }

        // Halve damage if defending Pokemon is resistant
        if (defendingPokemon.resistance.equals(type)) {
            damage /= 2;
        }

        // Double damage if defending Pokemon is weak
        if (defendingPokemon.weakness.equals(type)) {
            damage *= 2;
        }
        return damage;
    }

    public void recoverEnergy() { energy = Math.min(50, energy + 10); }

    public void resetEnergy() { energy = 50; }

    public void recoverHP() {
        hp = Math.min(maxHP, hp+20);
    }

    public boolean checkFainted() {
        return hp == 0;
    }

    public String toString() {
        /*
            Overriding toString method to print out all the stats.
         */
        String out = name + " " + hp + " " + type + " " + resistance + " " + weakness + " ";
        for (Attack attack : attacks) {
            out += " " + attack.toString();
        }
        return out;
    }
}