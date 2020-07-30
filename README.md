# DoubleHearts
Double Hearts is a double-deck version of the Hearts game on early Windows systems. It's called Gong Zhu alternatively, as you can find on [Wikipedia](https://en.wikipedia.org/wiki/Gong_Zhu). 

## Game Rule

Just like Hearts and Bridge, Double Hearts is a trick-taking card game, but it is based on two decks. It requires exactly 4 players to play.

In each deal, each player is dealt 26 cards randomly. They take turns in leading rounds, in which they play cards from their hands. Eventually, when their hands are cleared simultaneously, their scores are calculated and they initiate the next deal. 

In each round, the leader leads with one single card or a pair of identical cards, and the other players follow with the **same number** of cards. 

The one playing cards with the highest value wins the round, takes all cards in that round and leads the next round. 

### Leading and Following

 - Generally, each round should be lead with a single card (e.g., &#x2660;9) or a pair of identical cards (e.g. &#x2666;77). 
 - Generally, the followers must follow with **same number of** cards in the **same suit** as the leading card(s). 
    - If a follower's hand is empty in the leading suit, he can **discard**, i.e., play cards from another suit. 
       - If the leading cards are a pair (e.g., &#x2660;99) while the follower has only 1 card (e.g., &#x2660;K) in the leading suit, he must play the remaining card and select another card to discard. This mixed play will still be regarded as discarding.
    - If the round is led by a pair, the follower **must follow with a pair in the leading suit** when he has one. 
       
 - The first leader is a random &#x2663;2 owner who must lead the first round with one or two &#x2663;2(s). 
 - In the first round, when running out of the &#x2663; suit, the followers must not follow with &#x2666;J, &#x2660;Q or &#x2665;5-A.

### Decision of winning a trick

 - In the same suit, the card values are equivalent to their ranks: 2 < 3 < 4 < 5 < 6 < 7 < 8 < 9 < 10 < J < Q < K < A. 
 
 - A discard always has lower value than the leading play and the other following plays within the suit.
 
 - A pair of higher rank has higher value than another pair of lower rank in the same suit. 
 
 - A pair always have lower value than two different single cards, whose value as an entity is decided by the one of higher rank. 
 
 - In a round, when two players play cards of the same value, the first one wins. 

For instance, when a round is led by &#x2666;88, &#x2666;K &#x2665;A < &#x2666;88 < &#x2666;AA < &#x2666;23 < &#x2666;9Q (player after &#x2666;2Q) < &#x2666;2Q (player before &#x2666;9Q). If the players actually followed (&#x2666;K&#x2665;A), (&#x2666;2Q), (&#x2666;9Q) sequentially, the one who played &#x2666;2Q should take the scored &#x2665;A into his pocket. 

### Scoring
 
 - List of scored cards and their basic effects:
 
 | &#x2663;10 | &#x2666;J | &#x2660;Q | &#x2665;2-4 | &#x2665;5-10 | &#x2665;J | &#x2665;Q | &#x2665;K | &#x2665;A |
 | :--------: | :-------: | :-------: | :---------: | :----------: | :-------: | :-------: | :-------: | :-------: |
 | x2 | +100 | -100 | 0 | -10 | -20 | -30 | -40 | -50 |
 
 Clearly, most cards (&#x2660;Q and &#x2665; honors) are with penalty scores. The essence of this game is to avoid taking tricks containing these penalties. 
 
 - Generally, at the end of each hand, a player's score is given by first summing up scores of the scored cards (except &#x2663;10) he got from winning rounds, and multiply the sum with the multiplier decided by the (except &#x2663;10) cards he has. 
    - For instance, if a player got {&#x2663;10 10, &#x2660;Q, &#x2665;2 7 J}, his base score is -130, the score sum of {&#x2660;Q, &#x2665;2 7 J}. Since he has two &#x2663;10's, his final score is base score times **4**, which equals -520. 
    
 - Special cases:
    - A player got no scored cards (including &#x2665;2-4) except &#x2663;10: each &#x2663;10 is worth +50 points now as a reward of avoiding winning penalty tricks under doubled and even quadrapled risks.
    - **Small slam**: a player who gets all 26 hearts (including &#x2665;2-4) in one hand should turn all negative heart scores to positive. 
    - **Grand slam**: a player who gets all **scored cards** should turn all negative heart **and &#x2660;Q** scores to positive. 

### Additional Phases Besides Playing Rounds

#### Card Passing or Trading

After dealing all cards, each player chooses 3 cards to pass to another player. The number of hands dealt decides to whom to pass the cards. 

#### Exposure of Special Scored Cards

**&#x2663;10, &#x2666;J, &#x2660;Q** are considered special scored cards because they are all the unique scored cards in their respective suits. After card trading, the players choose to expose an arbitrary combination of the special cards in their hands. Exposed cards will be public and enjoy double effects:

| &#x2663;10 | &#x2666;J | &#x2660;Q |
| :--------: | :-------: | :-------: |
| x4 | +200 | -200 |

Note that there exists alternative rules where an exposure will cause all cards with the same face to be doubled. It is also allowed to expose &#x2665;A in some other rules, which results in doubled penalty of the entire heart suit. This, however, is considered imbalanced in this implementation, because you can easily see a score of less than -10,000 in a single deal. 

## Usage
Run server at port `32266`: (default port is `23366`)
```
java -jar DoubleHeartsServer.jar -p 32266
```

Run client in English and connect to server at IP address `142.857.428.571` (obviously fake) with port `32266`:
```
java -jar DoubleHeartsClient.jar -l en -a 142.857.428.571 -p 32266 
```

Enjoy!
