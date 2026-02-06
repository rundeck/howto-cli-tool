# Howto

"How do I [build/install/release] this source repo i just cloned?"

Just ask `how [to]`

```bash
$ how to
Found Actions:
________________________________________ 
build   : Build the project with Gradle
test    : Test the project with Gradle
install : Build and install locally with Gradle.
local   : Run local installation
release : Release the project.
pond    : Toss a coin in the pond!
________________________________________ 
tip: see more: how to [action]
tip: do the action: how do [action] [args]
tip: use how to -v for more information

```

Get more info with `how to [action]`.

```bash
$ how to build
build:

    Build the project with Gradle

    ./gradlew build

________________________________________ 
tip: do the thing with: how do [action] [args]
tip: or just: how [action] [args]
```

Run it with `how do` or just `how [action]`

```bash
$ how do pond
Today you will have:

A little luck.
```

## How do I install with homebrew?

1. Run `brew tap rundeck/howto-cli-tool`
2. Run `brew install howto`

## How does it work?

`how` uses various "detectors" to look for actions that the user could do.

1. It looks for a `howto.md` file (or name variation of that), or a `readme.md` file
   1. If there is a "how to" H1 section, it looks for H2s within that
      2. Each H2 section with a code block is treated as an action.
   2. Otherwise any H2 section with a title containing "how to" and a code block is treated as an action
2. Otherwise, it checks various build tools, such as:
   * Gradle
   * NPM
   * ... (add your own!)
3. It introspects available actions for the build tool(s) found

## Download

See the [Releases](https://github.com/rundeck/howto-cli-tool/releases) section.


# Writing a Howto doc

Create a "howto.md" or "readme.md". You can also name the file with a variation such as `howto.markdown` or just `HOWTO` (but the file must use Markdown formatting). 

If `howto` sees both a howto.md and readme.md, it will use howto.md.

Any H2 section with the words "how to" in the title that contains a code block will be treated as an action.

You can also use a H1 section where the title has the words "how to", in which case all H2 sections with code blocks below it will be selected.

The heading of the H2 section will be the name of the action.

    ## Build

Within the H2 section, use a code block (indented of fenced), or simple code section to define the action to invoke.

    ```
    make dist
    ```

If you want to accept arguments use `"${@}"` (for bash). Pass arguments using `how do [action] -- [args]` or `how [action] -- [args]`.

    `sh build.sh "${@}"`

If you want to reference the directory containing the HOWTO doc that is used, an environment variable is set: `DIR`

    ```
    cat "${DIR}/somefile"
    ```

# Using other build tools

If no "howto.md" or `# Howto` section in the readme is found,
Howto will look for common build tools, and try to introspect them.

Currently these tools are supported:

- [x] NPM - looks for the `package.json` file and any "scripts" defined within it
- [x] Gradle(w) - looks for `build.gradle` file and any parent dir containing `settings.gradle` and `gradlew`, then lists available tasks with `gradlew tasks`

TODO:

- [ ] Gradlew.bat wrapper for gradle (windows)
- [ ] `gradle` without a wrapper
- [ ] Maven
- [ ] Your favorite build tool...
