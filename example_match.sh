#!/bin/bash

# Define the URL
URL="http://localhost:8080/games/1f3bea4e-48e3-4b7b-8ff8-6287f201b510/moves"

# Initial data payloads
declare -a DATA=(
  '{"side": "WHITE", "move": "9-14",  "playerId": 1}'
  '{"side": "BLACK", "move": "2-17",  "playerId": 2}'
  '{"side": "WHITE", "move": "11-15", "playerId": 1}'
  '{"side": "BLACK", "move": "5-22",  "playerId": 2}'
  '{"side": "WHITE", "move": "15-19", "playerId": 1}'
  '{"side": "BLACK", "move": "3x16",  "playerId": 2}'
  '{"side": "WHITE", "move": "12x19", "playerId": 1}'
  '{"side": "BLACK", "move": "4x15",  "playerId": 2}'
  '{"side": "WHITE", "move": "10x19", "playerId": 1}'
  '{"side": "BLACK", "move": "7x10",  "playerId": 2}'
  '{"side": "WHITE", "move": "6x15",  "playerId": 1}'
  '{"side": "BLACK", "move": "1-17",  "playerId": 2}'
  '{"side": "WHITE", "move": "5-9",   "playerId": 1}'
  '{"side": "BLACK", "move": "9-25",  "playerId": 2}'
  '{"side": "WHITE", "move": "8-12",  "playerId": 1}'
  '{"side": "BLACK", "move": "5-21",  "playerId": 2}'
  '{"side": "WHITE", "move": "7-10",  "playerId": 1}'
  '{"side": "BLACK", "move": "7-13",  "playerId": 2}'
  '{"side": "WHITE", "move": "1-6",   "playerId": 1}'
  '{"side": "BLACK", "move": "7-24",  "playerId": 2}'
  '{"side": "WHITE", "move": "4-8",   "playerId": 1}'
  '{"side": "BLACK", "move": "2-27",  "playerId": 2}'
  '{"side": "WHITE", "move": "9-14",  "playerId": 1}'
  '{"side": "BLACK", "move": "7-23",  "playerId": 2}'
  '{"side": "WHITE", "move": "3-7",   "playerId": 1}'
  '{"side": "BLACK", "move": "3x16",  "playerId": 2}'
  '{"side": "WHITE", "move": "12x19", "playerId": 1}'
  '{"side": "BLACK", "move": "2-17",  "playerId": 2}'
  '{"side": "WHITE", "move": "7-11",  "playerId": 1}'
  '{"side": "BLACK", "move": "6-23",  "playerId": 2}'
  '{"side": "WHITE", "move": "19x26", "playerId": 1}'
  '{"side": "BLACK", "move": "0x23",  "playerId": 2}'
  '{"side": "WHITE", "move": "8-12",  "playerId": 1}'
  '{"side": "BLACK", "move": "4-20",  "playerId": 2}'
  '{"side": "WHITE", "move": "15-18", "playerId": 1}'
  '{"side": "BLACK", "move": "3-19",  "playerId": 2}'
  '{"side": "WHITE", "move": "11-15", "playerId": 1}'
  '{"side": "BLACK", "move": "0-16",  "playerId": 2}'
  '{"side": "WHITE", "move": "15x24", "playerId": 1}'
  '{"side": "BLACK", "move": "8x19",  "playerId": 2}'
  '{"side": "WHITE", "move": "2-7",   "playerId": 1}'
  '{"side": "BLACK", "move": "1-26",  "playerId": 2}'
  '{"side": "WHITE", "move": "18-23", "playerId": 1}'
  '{"side": "BLACK", "move": "6-22",  "playerId": 2}'
  '{"side": "WHITE", "move": "23-27", "playerId": 1}'
  '{"side": "BLACK", "move": "6-11",  "playerId": 2}'
  '{"side": "WHITE", "move": "7x23",  "playerId": 1}'
  '{"side": "BLACK", "move": "2-18",  "playerId": 2}'
)

NUM_REQUESTS=${#DATA[@]}

for ((i=0; i<NUM_REQUESTS; i++))
do
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
