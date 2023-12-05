# Knowledgebase

```
    __ __                         __           __              __
   / //_/ ____   ____  _      __ / /___   ____/ /____ _ ___   / /_   ____ _ _____ ___
  / ,<   / __ \ / __ \| | /| / // // _ \ / __  // __ `// _ \ / __ \ / __ `// ___// _ \
 / /| | / / / // /_/ /| |/ |/ // //  __// /_/ // /_/ //  __// /_/ // /_/ /(__  )/  __/
/_/ |_|/_/ /_/ \____/ |__/|__//_/ \___/ \__,_/ \__, / \___//_.___/ \__,_//____/ \___/
                                              /____/
```

This repo contains parts of documentations, examples and workarounds for different technologies that I want to save.

## Contents

- [Java](./java)

## Setup dev env (for pre-commit checks)

1. Install `python 3.11+` [\[download\]](https://www.python.org/downloads/release/python-3117/)
1. Check installation with command `python --version`
1. Add `pre-commit` dependency
   You can use global python dependencies, but we recommend to use virtual env
   Variant 1: virtual env (recommended)

```shell
python -m venv venv
./venv/Scripts/activate     # for Windows
source venv/bin/activate     # for *NIX
python -m pip install pre-commit==3.4.0
```

Variant 2: global

```shell
python -m pip install pre-commit==3.4.0
```

1. Install `pre-commit` hook

```shell
pre-commit install -f
```

### Note: set LF endings for project in git

```
git config --local core.autocrlf false
```
