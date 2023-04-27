#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
import os
import openai
import sys
openai.api_key = "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"
m = list()
try:
	file = open(sys.argv[1], "r")
	temp = {"role": "assistant", "content": file.readline()}
	for string in file:
		m.append(temp)
		temp = {"role": "assistant", "content": string}
	temp["role"] = "user"
	m.append(temp)
except:
	print("Something went wrong...")
	sys.exit(0)


completion = openai.ChatCompletion.create(
	model = "gpt-3.5-turbo",
	messages = m
)

print(completion.choices[0].message.content)
