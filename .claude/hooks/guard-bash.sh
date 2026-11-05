#!/bin/bash
# PreToolUse-hook для инструмента Bash.
# Claude Code передаёт JSON в stdin; команда лежит в .tool_input.command.
# Код выхода 2 = заблокировать вызов, текст из stderr получает агент.
# Нужен jq (brew install jq).

cmd=$(jq -r '.tool_input.command // empty')

# Опасные команды и попытки прочитать секреты через shell.
if echo "$cmd" | grep -qE 'git push.*(--force|--force-with-lease| -f( |$))|git reset --hard|git clean -[a-z]*f|rm -rf +(/|~|\$HOME)( |$)|(cat|less|more|head|tail|grep|sed|awk) .*\.env'; then
  echo "Заблокировано правилом проекта (.claude/hooks/guard-bash.sh): $cmd" >&2
  echo "Если команда действительно нужна, выполните её сами в терминале." >&2
  exit 2
fi

exit 0
