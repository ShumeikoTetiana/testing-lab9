# ЛР9 — CI/CD (GitHub Actions), SUT B варіант 17

## Що де лежить
| Файл | Для чого |
|---|---|
| `.github/workflows/ci.yml` | CI-конфігурація: trigger, jobs build → unit-tests → integration-tests, збереження звітів |
| `pom.xml` | додано профілі `unit` і `integration` (які тести запускає кожен job) |
| `src/test/java/ua/course/store/OrderServiceUnitTest.java` | 6 unit-тестів (Mockito, без БД): персональна знижка, межі, округлення, кількість, відмова policy |
| `src/test/java/ua/course/store/OrderRegressionIT.java` | integration-тести з реальним PostgreSQL (Testcontainers), стабільна версія з ЛР8 |

## Як запустити (один раз)
1. На github.com створи **порожній** репозиторій (наприклад `testing-lab9`), без README.
2. У терміналі з папки цього проєкту (там, де `pom.xml`):
```
git init
git add .
git commit -m "LR9: CI pipeline"
git branch -M main
git remote add origin https://github.com/<твій-логін>/testing-lab9.git
git push -u origin main
```
3. Відкрий репозиторій → вкладка **Actions** → workflow **CI**. Pipeline стартує сам після push.
   Має бути 3 зелені jobs: build → unit-tests → integration-tests.
4. Відкрий run → унизу блок **Artifacts**: `unit-test-report`, `integration-test-report` (JUnit XML + txt).

## Сценарій 2: failed pipeline
```
git checkout -b broken
```
У `OrderServiceUnitTest.java` у тесті `discountAppliedAtThreshold` зміни `160000` на `150000`, потім:
```
git commit -am "Навмисна помилка в тесті"
git push -u origin broken
```
Actions → run гілки `broken`: job **unit-tests** червоний, **integration-tests** пропущений (бо `needs`).
Причину видно в лозі кроку «Unit tests»: `expected: <150000> but was: <160000>`, і в artifact `unit-test-report`.
Додатково: Pull request `broken` → `main` теж запустить pipeline (trigger `pull_request`).

Повернення до зеленого: `git checkout main` (main не змінювався, його pipeline залишається зеленим).

## Важливо
- Немає `continue-on-error` і `|| true`: якщо тест падає, job і весь pipeline падають.
- `if: always()` біля upload-artifact — щоб звіт зберігався і при падінні тестів.
