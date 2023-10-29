#!/bin/bash

START_RESPONSE=$(curl -s -X POST -H "Content-Type: application/json" -d '{"playerId": 1}' http://localhost:8080/games)

GAME_ID=$(echo "$START_RESPONSE" | jq -r '.gameId')

echo "$GAME_ID"

URL=$(echo http://localhost:8080/games/"${GAME_ID}"/moves)

echo "$URL"

# Initial data payloads
declare -a DATA=(
  '{"side": "BLACK", "move": "9-14", "state": {"black":[1,2,3,4,5,6,7,8,9,10,11,12], "white":[21,22,23,24,25,26,27,28,29,30,31,32]}, "playerId": 1}'
  '{"side": "WHITE", "move": "22-17", "playerId": 2}'
  '{"side": "BLACK", "move": "11-15", "playerId": 1}'
  '{"side": "WHITE", "move": "25-22", "playerId": 2}'
  '{"side": "BLACK", "move": "15-19", "playerId": 1}'
  '{"side": "WHITE", "move": "23x16", "playerId": 2}'
  '{"side": "BLACK", "move": "12x19", "playerId": 1}'
  '{"side": "WHITE", "move": "24x15", "playerId": 2}'
  '{"side": "BLACK", "move": "10x19", "playerId": 1}'
  '{"side": "WHITE", "move": "17x10", "playerId": 2}'
  '{"side": "BLACK", "move": "6x15", "playerId": 1}'
  '{"side": "WHITE", "move": "21-17", "playerId": 2}'
  '{"side": "BLACK", "move": "5-9",   "playerId": 1}'
  '{"side": "WHITE", "move": "29-25", "playerId": 2}'
  '{"side": "BLACK", "move": "8-12",  "playerId": 1}'
  '{"side": "WHITE", "move": "25-21", "playerId": 2}'
  '{"side": "BLACK", "move": "7-10",  "playerId": 1}'
  '{"side": "WHITE", "move": "17-13", "playerId": 2}'
  '{"side": "BLACK", "move": "1-6", "playerId": 1}'
  '{"side": "WHITE", "move": "27-24", "playerId": 2}'
  '{"side": "BLACK", "move": "4-8", "playerId": 1}'
  '{"side": "WHITE", "move": "32-27", "playerId": 2}'
  '{"side": "BLACK", "move": "9-14", "playerId": 1}'
  '{"side": "WHITE", "move": "27-23", "playerId": 2}'
  '{"side": "BLACK", "move": "3-7", "playerId": 1}'
  '{"side": "WHITE", "move": "23x16", "playerId": 2}'
  '{"side": "BLACK", "move": "12x19", "playerId": 1}'
  '{"side": "WHITE", "move": "22-17", "playerId": 2}'
  '{"side": "BLACK", "move": "7-11", "playerId": 1}'
  '{"side": "WHITE", "move": "26-23", "playerId": 2}'
  '{"side": "BLACK", "move": "19x26", "playerId": 1}'
  '{"side": "WHITE", "move": "30x23", "playerId": 2}'
  '{"side": "BLACK", "move": "8-12", "playerId": 1}'
  '{"side": "WHITE", "move": "24-20", "playerId": 2}'
  '{"side": "BLACK", "move": "15-18", "playerId": 1}'
  '{"side": "WHITE", "move": "23-19", "playerId": 2}'
  '{"side": "BLACK", "move": "11-15", "playerId": 1}'
  '{"side": "WHITE", "move": "20-16", "playerId": 2}'
  '{"side": "BLACK", "move": "15x24", "playerId": 1}'
  '{"side": "WHITE", "move": "28x19", "playerId": 2}'
  '{"side": "BLACK", "move": "2-7", "playerId": 1}'
  '{"side": "WHITE", "move": "31-26", "playerId": 2}'
  '{"side": "BLACK", "move": "18-23", "playerId": 1}'
  '{"side": "WHITE", "move": "26-22", "playerId": 2}'
  '{"side": "BLACK", "move": "23-27", "playerId": 1}'
  '{"side": "WHITE", "move": "16-11", "playerId": 2}'
  '{"side": "BLACK", "move": "7x23", "playerId": 1}'
  '{"side": "WHITE", "move": "22-18", "playerId": 2}'
)

NUM_REQUESTS=${#DATA[@]}

for ((i=0; i<NUM_REQUESTS; i++))
do
    echo "${DATA[i]}"
    echo "${URL}"
    # Send a PUT request with the corresponding data payload and capture the response
    RESPONSE=$(curl -s -X PUT -H "Content-Type: application/json" -d "${DATA[i]}" "$URL")

    echo $RESPONSE

    # Extract the 'state' object from the response
    NEW_STATE=$(echo "$RESPONSE" | jq '.state')

    # If this is not the last iteration, update the 'state' object for the next request
    if [ $i -lt $((NUM_REQUESTS-1)) ]; then
        # Construct the next data payload with the updated 'state' object
        NEXT_PAYLOAD=$(echo "${DATA[i+1]}" | jq --argjson state "$NEW_STATE" '. + {state: $state}')
        DATA[i+1]=$NEXT_PAYLOAD

        # Sleep for a specified interval before the next request
        sleep 5  # Sleep for 10 seconds, adjust this value to your preferred interval
    fi
done
