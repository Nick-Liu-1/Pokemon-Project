/*
Main.java
Nick Liu
ICS4U-01
Main game class implementing the Pokemon Arena, allowing the user to play the game.
 */

import java.util.*;
import java.io.*;

public class Main {
    private static ArrayList<Pokemon> pokemon = new ArrayList<>();  // Pool of Pokemon
    private static ArrayList<Pokemon> trainerPokemon = new ArrayList<>();  // Pokemon selected by trainer

    private static String name;  // Name of trainer

    // Constants for user inputs
    public final static int BACK = 0;
    public final static int ATTACK = 1;
    public final static int RETREAT = 2;
    public final static int PASS = 3;

    // Size of spaces for outputs
    private static int maxsize = 12;
    private static int maxAttackSize = 12;
    private static int maxTypeSize = 12;

    private static Pokemon active;  // User pokemon in battle
    private static Pokemon enemy;  // Enemy pokemon in battle

    public static void main(String[] args) {
        load("pokemon.txt");  // Load all of the pokemon
        init();  // Initialize the game
        newBattle();  // Start the battles!
    }

    public static int getInput(int low, int high, String errorMessage) {
        /*
            Prompts user for inputs until a valid input is given. A valid input is one that is an integer that falls in
            within [low, high]. If the input is not valid, it will print the error message and ask for the input again.
         */
        Scanner sc = new Scanner(System.in);
        int choice = low - 1;  // Set it to 1 less than low so that if choice is non-numeric it is out of bounds and invalid

        do {
            if (sc.hasNextInt()) {
                choice = sc.nextInt();  // Get input from user
            }
            else {
                sc.next();  // Take user's invalid input, but do not do anything with it
            }

            if (choice < low || choice > high) {  // If the choice is not within the boundaries it is invalid
                System.out.println(errorMessage);
            }
        } while (choice < low || choice > high);
        return choice;
    }

    public static void getTrainerName() {
        /*
            Gets the name from the user.
        */
        Scanner sc = new Scanner(System.in);
        System.out.println("WELCOME TO THE POKEMON ARENA! PLEASE ENTER YOUR NAME: ");
        name = sc.nextLine();
    }

    public static void init() {
        /*
            Initializes the game by loading the Pokemon and allowing the user to select their team
         */
        getTrainerName();  // Get name from user

        // Iterating through all of the Pokemon and printing them out for the menu
        for (int i = 0; i < pokemon.size(); i++) {
            Pokemon p = pokemon.get(i);
            System.out.printf("(%03d) %-"+maxsize+"s Type: %-"+maxTypeSize+"s HP: %-4d\n", i+1, p.getName(), p.getType(), p.getHp());
        }

        System.out.println("Welcome Trainer " + name + "! Select four of the above Pokemon by typing in the corresponding numbers, one per line:");

        ArrayList<Integer> choices = new ArrayList<>();  // List of Pokemon selected by user
        // Allow user to select 4 Pokemon to add to their team
        for (int i = 0; i < 4; i++) {
            int choice;  // Current Pokemon choice
            // Prompt user for a selection until they choose a Pokemon that is not already in their entourage
            do {
                choice = getInput(1, pokemon.size(), "Please enter a valid number to select your Pokemon.");
                if (choices.contains(choice)) {  // Lets user know if they selected a Pokemon already selected
                    System.out.println(pokemon.get(choice-1).getName() + " has already been selected. Please choose a different Pokemon.");
                }
            } while (choices.contains(choice));

            choices.add(choice);
            trainerPokemon.add(pokemon.get(choice-1));  // Add selection to trainerPokemon arraylist
            System.out.println("You have chosen " + pokemon.get(choice-1).getName() + "!");
        }

        // Remove selected Pokemon from the pool
        for (Pokemon p: trainerPokemon) {
            pokemon.remove(p);
        }

        System.out.println("GET READY TO BATTLE!!!!!! -------------------------------------------------");

    }

    public static Pokemon selectPokemon() {
        /*
            In between battles, prompts user to select a Pokemon to put into battle. Returns the selected Pokemon.
         */
        int choice = getInput(1, trainerPokemon.size(), "Please enter a valid number to select your Pokemon.");
        return trainerPokemon.get(choice-1);  // Is index - 1 because it is printed out starting at 1
    }

    public static Pokemon selectRetreatPokemon() {
        /*
            Prompts user to select a Pokemon to switch in after retreating. Returns the selected Pokemon.
         */
        int choice = getInput(0, trainerPokemon.size(),"Please enter a valid number to select your Pokemon." );
        if (choice == 0) { return null; }

        return trainerPokemon.get(choice-1);
    }

    public static void printPokemon() {
        /*
            Prints out the user's Pokemon and their stats.
         */
        for (int i = 0; i < trainerPokemon.size(); i++) {
            Pokemon temp = trainerPokemon.get(i);
            System.out.printf("(%d) %-"+maxsize+"s  HP: %-3d  ENERGY: %-3d  TYPE: %s \n", i+1, temp.getName(), temp.getHp(), temp.getEnergy(), temp.getType());
        }
    }

    public static void newBattle() {
        /*
            Gets user to select their active Pokemon and randomly selects an enemy
            to fight. Starts a new battle.
         */

        // User selection
        System.out.println("Select which Pokemon use this battle:");
        printPokemon();
        active = selectPokemon();
        System.out.println(active.getName() + " I choose you!");

        // Replacing enemy
        enemy = pokemon.get(randint(0, pokemon.size()-1));
        System.out.println(enemy.getName() + " has come to challenge!");

        // Printing stats
        System.out.printf("%s  HP: %d  ENERGY: %d  TYPE: %s\n", active.getName(), active.getHp(), active.getEnergy(), active.getType());
        System.out.printf("%s  HP: %d  ENERGY: %d  TYPE: %s\n", enemy.getName(), enemy.getHp(), enemy.getEnergy(), enemy.getType());

        // Starting the new battle
        battle();
    }

    public static void battle() {
        /*
            Randomly determines who goes first and alternates between user and enemy, allowing them to take their turn.
         */
        boolean userFirst = randint(0, 1) == 1;  // Determines if user is first or second

        while (true) {
            if (userFirst) {
                boolean enemyDead = resolveUser();  // Resolving user turn
                if (enemyDead) {  // Exit loop when enemy is dead as battle is over
                    break;
                }
                resolveEnemy();  // Resolve enemy turn
            }
            else {  // Same as above but does enemy turn before user
                resolveEnemy();
                boolean enemyDead = resolveUser();
                if (enemyDead) {
                    break;
                }
            }

            // Restoring 10 ENERGY to all Pokemon at end of round
            System.out.println("10 ENERGY has been restored to all Pokemon!");
            for (Pokemon p : trainerPokemon) {
                p.recoverEnergy();
            }
            enemy.recoverEnergy();
        }
    }

    public static boolean resolveUser() {
        /*
            Checks if user is stunned and if not, allow them to take their turn. After their turn is over check if
            enemy fainted.
         */
        boolean battleDone = false;

        // If user is stunned, skip their turn
        if (active.getStunned()) {
            System.out.println();  // Printing empty line to make things easier to read
            System.out.println(active.getName() + " is stunned! It cannot attack or retreat!");
        }

        // If user is not stunned, they can carry out their turn
        else {
            userAction();
        }

        // If enemy faints, battle is done
        if (enemy.checkFainted()) {
            System.out.println(enemy.getName() + " has fainted!");
            pokemon.remove(enemy);  // Remove enemy from pool
            if (pokemon.size() == 0) {  // Check if all Pokemon have been defeated
                gameOver(true);
                return true;
            }

            endBattle();
            battleDone = true;
        }

        active.unStun();
        return battleDone;
    }

    public static void resolveEnemy() {
        /*
            Checks if enemy is stunned and if not, allow them to take their turn. After their turn is over check if
            user fainted.
         */
        // If enemy is stunned, skip their turn
        if (enemy.getStunned()) {
            System.out.println();
            System.out.println(enemy.getName() + " is stunned! It cannot attack or retreat!");

        }
        // If enemy is not stunned, they can carry out their turn
        else {
            enemyAction();
        }

        // Check if user Pokemon faints
        if (active.checkFainted()) {
            System.out.println(active.getName() + " has fainted!");
            trainerPokemon.remove(active);  // remove pokemon from trainer's pool
            if (trainerPokemon.size() == 0) {
                gameOver(false);
                return;
            }

            // Allow user to put in a new Pokemon
            System.out.println();
            System.out.println("Select which Pokemon to use:");
            printPokemon();
            active = selectPokemon();  // Allow user to select new Pokemon
            System.out.println(active.getName() + " I choose you!");
        }
        enemy.unStun();
    }

    public static void userAction() {
        /*
            Allows user to either attack, retreat, or pass and carries out those actions
         */
        System.out.println();  // Printing empty line to make things easier to read
        System.out.printf("%s  HP: %d  ENERGY: %d  TYPE: %s\n", active.getName(), active.getHp(), active.getEnergy(), active.getType());
        System.out.printf("(1) %-12s(2) %-12s(3) %-12s\n", "Attack", "Retreat", "Pass");  // Printing user options

        // Getting action from user
        int action = getInput(1, 3, "Please enter a valid number to select your action.");

        if (action == ATTACK) {
            int selectedAttack;  // Attack selected by user
            boolean success;  // If attack is successful or not

            // Prompt user until they choose an attack that is successful, or returns to main menu
            do {
                // Printing out all of the active Pokemon's attacks
                for (int i = 0; i < active.getAttacks().size(); i++) {
                    Pokemon.Attack temp = active.getAttacks().get(i);
                    System.out.printf("(%d) %-"+maxAttackSize+"s  Cost: %-3d  Damage:  %-3d  Special:  %s\n", i+1, temp.getName(), temp.getCost(), temp.getDamage(), temp.getSpecial());
                }
                System.out.printf("(%d) %-"+maxAttackSize+"s\n", 0, "Back");

                // Get user selection of attack
                selectedAttack = getInput(0, active.getAttacks().size(),"Please enter a valid number to select your move.");

                if (selectedAttack == BACK) {  // Exiting attack menu and returning to main menu
                    userAction();
                    return;
                }

                // Carrying out the attack. If not enough energy to use then variable will be false
                success = active.attack(active.getAttacks().get(selectedAttack - 1), enemy);

                if (!success) {
                    System.out.println("You do not have enough energy to use this move.");
                }

            } while (!success);

            // Printing stats
            System.out.printf("%s  HP: %d  ENERGY: %d  TYPE: %s\n", active.getName(), active.getHp(), active.getEnergy(), active.getType());
            System.out.printf("%s  HP: %d  ENERGY: %d  TYPE: %s\n", enemy.getName(), enemy.getHp(), enemy.getEnergy(), enemy.getType());
        }
        else if (action == RETREAT) {
            if (trainerPokemon.size() == 1) {  // User cannot retreat if they only have 1 Pokemon remaining
                System.out.println("You cannot retreat!");
                userAction();  // Let them choose a different action
                return;
            }

            Pokemon temp;  // Temporary Pokemon that will get swapped in
            // Prompt user to select a Pokemon to swap in until they choose a valid one (a Pokemon that is not the active one)
            do {
                System.out.println("Select which Pokemon to swap in.");
                printPokemon();
                System.out.printf("(0) %-"+maxsize+"s\n", "Back");
                temp = selectRetreatPokemon();
                if (temp == active) {  // User can't swap in already active Pokemon
                    System.out.println(temp.getName() +  " is already active!");
                }
            } while (temp == active);

            if (temp != null) {  // Active Pokemon becomes temp
                active = temp;
                System.out.println(active.getName() + " I choose you!");
            }
            else { // If the user selected 0, temp will be null and the user will be sent back to main menu.
                userAction();
                return;
            }
        }

        if (action == PASS) {  // User passing turn
            System.out.println(active.getName() + " passes its turn!");
        }

    }

    public static void enemyAction() {
        /*
            Randomly selects an attack for the enemy Pokemon and attacks the active Pokemon.s
         */
        System.out.println();  // Empty line to make things easier to read.
        boolean canAttack = false;  // Whether or not it is possible for enemy to attack
        // Iterates through the enemy's attacks and if there exists one that has a cost <= the the enemy's energy, it it possible to attack
        for (int i = 0; i < enemy.getAttacks().size(); i++) {
            if (enemy.getAttacks().get(i).getCost() <= enemy.getEnergy()) {
                canAttack = true;
                break;
            }
        }
        int attack;  // Randomly selected attack index
        boolean success;  // If attack can be used or not
        if (canAttack) {
            // Randomly generate an attack until one that can be used is generated
            do {
                attack = randint(0, enemy.getAttacks().size()-1);
                success = enemy.attack(enemy.getAttacks().get(attack), active);
            } while (!success);
        }
        else {  // If enemy cannot attack they pass their turn
            System.out.println(enemy.getName() + " passes its turn!");
        }
        // Printing stats
        System.out.printf("%s  HP: %d  ENERGY: %d  TYPE: %s\n", active.getName(), active.getHp(), active.getEnergy(), active.getType());
        System.out.printf("%s  HP: %d  ENERGY: %d  TYPE: %s\n", enemy.getName(), enemy.getHp(), enemy.getEnergy(), enemy.getType());
    }


    public static void endBattle () {
        /*
            Removes disables and stuns, removes the fainted Pokemon from the respective pool, and resolves the end
            of the battle.
         */

        // Restoring 10 ENERGY to all Pokemon at end of round
        System.out.println("10 ENERGY has been restored to all Pokemon!");
        for (Pokemon p : trainerPokemon) {
            p.recoverEnergy();
        }

        // Resetting energy
        for (Pokemon p: trainerPokemon) { p.resetEnergy(); }

        // Restore health to all of user's Pokemon
        System.out.println("END OF BATTLE! 20 HP has been restored to all of your remaining Pokemon!");
        for (Pokemon p: trainerPokemon) {
            p.recoverHP();
        }

        // Undisable all user Pokemon
        for (Pokemon p: trainerPokemon) {
            p.unDisable();
        }

        System.out.println();
        newBattle();  // Start new battle
    }

    public static void gameOver(boolean win) {
        /*
            Let's user know if they won/lost and gives them the option to play again or exit.
         */

        System.out.println();
        // Printing the respective messages if user won/lost
        if (win) {
            System.out.println("GAME OVER! " + name + " IS THE TRAINER SUPREME!");
        }
        else {
            System.out.println("GAME OVER! YOU LOSE!");
        }

        System.out.println("(1) PLAY AGAIN  (2) EXIT");

        // Prompting user for input until they make a valid selection
        int choice = getInput(1, 2, "Please make a valid selection.");

        if (choice == 1) {  // Play again
            // Resetting everything for the new game
            pokemon.clear();
            trainerPokemon.clear();
            active = null;
            enemy = null;
            name = null;

            // Starting the new game
            load("pokemon.txt");
            init();
            newBattle();
        }
        else {  // Exit
            System.exit(0);  // Exiting the program
        }
    }

    public static void load(String file) {
        /*
            Read the file passed into the method and turns it into an ArrayList of Pokemon objects.
         */
        try {
            Scanner inFile = new Scanner(new BufferedReader(new FileReader(file)));
            int n = Integer.parseInt(inFile.nextLine());  // Number of Pokemon

            // Iterating through all lines and parsing the data, creating Pokemon objects with the data
            for (int i = 0; i < n; i++) {
                String[] line = inFile.nextLine().split(",");  // Line of stats

                // Getting basic data
                String name = line[0];
                maxsize = Math.max(maxsize, name.length());  // Modify maxsize for spaces if necessary
                int hp = Integer.parseInt(line[1]);
                String type = line[2];
                String resistance = line[3];
                String weakness = line[4];

                ArrayList<ArrayList<String>> attacks = new ArrayList<>();  // Arraylist of attacks or current Pokemon

                // Getting attacks
                int numAttacks = Integer.parseInt(line[5]);
                for (int k = 0; k < numAttacks; k++) {
                    ArrayList<String> attack = new ArrayList<>();  // ArrayList of attack data for a specific attack
                    for (int ii = 1; ii <= 4; ii++) {  // Adding attack data to arraylist
                        attack.add(line[5 + 4*k + ii]);
                    }
                    // Modifiying sizes for spaces
                    maxAttackSize = Math.max(maxAttackSize, line[5 + 4*k + 1].length());
                    maxTypeSize = Math.max(maxTypeSize, line[5+4*k + 2].length());
                    attacks.add(attack);
                }

                pokemon.add(new Pokemon(name, hp, type, resistance, weakness, attacks));  // Creating a new Pokemon and adding it to the pool
            }
        }
        catch (FileNotFoundException e) {  // In case file is not found
            e.printStackTrace();
        }
    }

    public static int randint(int low, int high){
        /*
            Returns a random integer on the interval [low, high].
        */
        return (int) (Math.random()*(high-low+1)+low);
    }
}