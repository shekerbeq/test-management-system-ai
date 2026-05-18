# Prompt шаблондары

AI генерациясы `PromptBuilder` арқылы құралады. Негізгі параметрлер:

- `topic` - тест тақырыбы
- `difficulty` - Easy, Medium, Hard
- `questionType` - Single, Multiple, Mixed, Open
- `language` - kk, ru, en
- `count` - сұрақ саны

LLM жауабы JSON форматында қайтуы тиіс:

```json
[
  {
    "question": "Сұрақ мәтіні",
    "options": ["A", "B", "C", "D"],
    "correctIndices": [0],
    "correct": 0,
    "open": false
  }
]
```
