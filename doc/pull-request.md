# automating PRs
```
pr() {
  local repo_name="$(basename $(git rev-parse --show-toplevel))"
  local current_branch="$(git rev-parse --abbrev-ref HEAD)"

  local pr="https://github.com/sqsp/$repo_name/compare/$current_branch?expand=1"

  case "$repo_name" in
    "clozxing") pr="${pr}&template=pr_template.md" ;;
  esac

  open "$pr"
}
```

