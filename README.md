# trivial-ai-voice-chat

A basic voice-chat library meant to be incorporated into other applications, or played with directly. I dunno. Don't judge me.

## Usage


- `[ca.inaimathi/trivial-ai-voice-chat "0.0.0"]`
- In `repl`, do

```
trivial-ai-voice-chat.core> (def CHT (model/mk-chat :dir "./hello-world-chat"))
#'trivial-ai-voice-chat.core/CHT
trivial-ai-voice-chat.core> CHT
{:history #<Atom@5fd3c6ab: []>, :dir "./hello-world-chat"}
trivial-ai-voice-chat.core> (interact! CHT)
>> Hello, world. This is my first chat. Let's hope it works.
Hello! Welcome to the chat. I'm an AI assistant here to help and chat with you. How can I assist you today?
nil
trivial-ai-voice-chat.core>
```

(You should have been able to hear what you're expecting. Except that by default, this chat reads the robots' response at 1.5x speed.)

## License

Copyright © 2023 inaimathi

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
