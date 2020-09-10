# Raffle Trivia
This app consists of a multiple-choice trivia game for raffle tickets deployed on 20 Kindle Fires at  <a href="https://www.foxcroft.org/news-detail?pk=1077158&fromId=226003">Foxcroft's STEM Challenge: Galaxy Trek</a> (sponsored by Stryker) in February 2020.

At the STEM Challenge, 20 teams of girls from local middle and high schools compete for prizes (and glory) in STEM events designed to test their wits. Alongside the main events, each team has a Kindle fire with around 100 trivia questions designed to test their <i>knowledge.</i>

## What's in this repo?

In order to display the real meat of my programming work, I've uploaded the program files as `Raffle.zip` and moved the primary java files into the top-level directory for easy access. These are `Category.java`, `Question.java`, `TriviaEngine.java`, and `TriviaActivity.java`. I've also provided some samples to show the formatting of `pins.tsv` and `questions.tsv` in order to show what type of data the app reads in.

## Goals

The app is designed to accomplish a handful of tasks:

1. Encourage mingling between teams from different schools.
2. Provide a reusable format for both middle-school and high-school events, and for future years.
3. Provide edutainment during downtime between events and at meals.
4. Give teams a low-pressure opportunity to earn raffle tickets (every correct answer earned each team member a raffle ticket, and from the sheer number of questions every team was guaranteed at least one)
5. Have the app track raffle tickets so we don't have to!

### Goal #1: Team Mingling
Encouraging mingling required us to be clever. In the previous year's app (where the theme was "Pandemic") we had teams swap "interaction PINs" in order to "infect" each other, and the overarching puzzle at the end of the competition involved contact-tracing to discover which team was "patient zero." The development process for the Pandemic app was significantly more painstaking––I used it as an excuse to learn MIT App Inventor, which required hours and hours of dragging and dropping blocks to implement the logic I wanted. Additionally, we did not have reliable Wi-Fi at the event, so all "interactions" had to be accomplished without any networking capabilities.

For Galaxy Trek, I decided to create a format that could be applied to all future events (because "contact tracing" was logic specific to the "pandemic" theme). The goal was to provide low-pressure icebreakers for teams to interact with each other, so we created a set of icebreaker questions about all of the schools involved in the event, which ideally could only be answered by a team that went to that school. An example of this is a question encouraging teams to talk to the engineers representing Stryker, the corporate sponsor:

<b>Starter</b>	<i>Where is Stryker's corporate HQ located? (Don't know? Find someone from Stryker and ask!)</i>
- Kalamazoo, Michigan	
- Altoona, Pennsylvania	
- Milwaukee, Wisconsin	
- Duluth, Minnesota
  
The "Starter" category is the first category available when teams log into the app with their unique PIN (discussed below), and answering Starter questions correctly both earns the team a raffle ticket and unlocks another category. The categories we included in Galaxy Trek, in addition to Starter, were:

- Astrobiology
- Astrophysics
- Computer Science
- Constellations
- Famous Scientists
- Planetary Science
- Science Fiction
- Space Missions
- The Solar System
- Stars

### Goal 2: Reusable Format
In previous years, we would either ask teams to keep track of their own tickets, or have a completely different app for morning and afternoon teams. In order to avoid a complete overhaul of the format from year-to-year, and to avoid having to install multiple apps (and risk teams logging into the wrong one), I designed this app to read all of the important data from spreadsheets. In particular, we've got spreadsheets for:

- Team names and PINs. There is a morning competition and an afternoon competition, so we reused team names from the morning in the afternoon. By giving different PINs to the morning and afternoon teams, we can create unique save files and allow everyone to log into the same app. `pins.tsv` contains the team names and pins from the 2020 event. The first column contains the team names, the second column contains the morning PINs, and the third column contains the afternoon PINs. Team names and PINs are controlled completely by this spreadsheet, so themed names can be updated every year just by changing the first column.

- Questions. In previous years, questions were hard-coded directly into the app. In this year, and for future years, I wanted to be able to collect questions in a spreadsheet (collaborating with the department on Google Sheets), then swap out the spreadsheet to update the app. Questions are saved in a tab-separated-value format, with an example as `questions.tsv`. The format of this TSV is:

1. First column: Category name. One of these categories should match the constant defined in TriviaEngine.java (line 27), which is currently `"Starter"`
2. Second column: Question text. May contain commas, but SHOULD NOT contain any tabs, as it is saved as a .tsv. Editing in spreadsheet software will prevent this.
3. Third column: Correct answer. Answer order will be shuffled when questions are presented.
4. Columns 4, 5, 6: Incorrect "distractor" answers. 

Instead of artwork, we went with an Instagram-style animated gradient background. Code for the background was adapted from <a href="https://www.youtube.com/watch?v=x_DXXGvyfh8&ab_channel=CodinginFlow">this video</a>.

### Goals 3, 4, 5: Trivia Game

The trivia game itself works like most randomized trivia games.

A question is randomly selected from an unlocked category that has unanswered questions and displayed.

Users can skip the question (and it will come back later) or answer it.

The answer itself is stored in a save file––tickets are calculated by comparing the user's selected answer with the answer and calculating the number of matches. An internal counter keeps track of how many tickets have been redeemed. This allows users to redeem tickets at any point in time and submit them for prizes that interest them. Tickets can be redeemed by faculty manning the raffle table logging in with a PIN, which updates the number of "tickets redeemed" to the current number of tickets earned.

New categories unlock either when a question from the Starter category is unlocked, or when there are no questions remaining in the unlocked categories.

When all questions have been answered, a message is displayed telling the team to redeem their raffle tickets at the raffle table.

## Data Structures

As I was teaching AP Computer Science A (OOP in Java) while developing this app, I made design choices that allowed me to reuse code in class (i.e. limiting myself to the APCSA subset of Java). I aimed to keep the code organized, documented, and relatively simple.

- <b>`Question`</b>: In addition to some constants for default values, this class contains a `String` for the question text, a `String` for the correct answer, a `String` for the user answer, and an array of `String`s for the answer choices. There are several constructors available––one accepts a row from the tsv that has already been tokenized into a `String` for each column, and another accepts the row as a raw `String` then does the tokenizing. Contains methods for checking the correct answer, checking to see if the question has already been answered, and shuffling answer choices, in addition to the expected setters and getters.

- <b>`Category`</b> contains an `ArrayList` of Question objects, in addition to a name for the category and whether or not the category is currently locked. In addition to adding questions to the question bank and providing randomized questions to the `TriviaEngine`, this class handles most counting operations––checking how many questions have answered, how many are available, and how many are correct. `Category` objects can also be locked and unlocked, to gradually roll out questions once teams have started mingling. If all questions have been answered, a default, dummy `Question` object is returned (which `TriviaEngine` interprets as the category having no questions available. 

- <b>`TriviaEngine`</b> holds an array list of `Category` objects. It loads questions into these categories either from the initial .tsv of questions, or from a save file where the user's answers are stored. The engine provides the main functionality for the scoring mechanism, such as totalling up the number of questions that are answered correctly and keeping track of the number of tickets that have been redeemed. It also passes `Question` objects from `Category` objects to the `TriviaActivity`, which displays them in the interface. The `TriviaEngine` class is also responsible for importing questions from the save files into the question bank, and writing save files.

- <b>`TriviaActivity`</b> contains all of the Android machinery. It is primarily responsible for setting up the View and GUI, collecting user input and passing it to the `TriviaEngine`, instantiating the `TriviaEngine`, and displaying questions for the user.

## Next Steps and Final Thoughts

Although I am no longer teaching at Foxcroft, I've made this app customizable for my department to use in future years. All that needs to be done is updating the spreadsheets (`pins` with team names, and the `question` spreadsheets with morning and afternoon questions), rebuilding the app, and reinstalling on devices.

Admittedly, this is not the most convenient way to go about this. I would love to implement functionality that links the app up with a Google account, so spreadsheets can be directly configured in the already-installed app, rather than by modifying the package itself.

I also would have loved to have implemented network functionality for scoring (such as communicating scores directly to the faculty member in charge of the raffle table), and potentially eliminated the need for physical tickets altogether by allowing individual team members to select their prizes in-app. But paper tickets are fun!

That said, I've kept as much of the app as generic as possible, so it can be used for a variety of purposes. For instance, it could be used as a review activity before a test in class, as long as the students are provided with a log-in PIN and the questions are uploaded. As it stands, it is not possible for users on separate devices to create collisions with the same PIN, but having separate PINs for each student would be nice.

I would LOVE feedback on this!
