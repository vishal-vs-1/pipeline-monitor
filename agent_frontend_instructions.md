# Gemini AI Instructions & Best Practices (Frontend)

These instructions govern how you (Gemini AI) should approach building the Vigilant CI/CD monitoring tool Frontend.

## Project Structure & Separation of Concerns

### Frontend (Angular)
- **Feature Modules / Standalone Components**: Group related components by feature (e.g., `dashboard`, `config`).
- **Core Module (`src/app/core`)**: Singleton services, interceptors, and guards.
- **Shared Module (`src/app/shared`)**: Reusable UI components, directives, and pipes.
- **Services**: UI components should strictly handle presentation. Data fetching, state management, and WebSocket communication must happen in dedicated Angular services.
- **Styling**: Use Tailwind CSS utility classes directly in templates for standard layouts, and daisyUI for complex components (buttons, cards, modals). Avoid writing custom vanilla CSS unless strictly necessary for custom micro-animations.

## Coding Best Practices
- **Strict Typing**: Leverage TypeScript strictly. Avoid using `any`.
- **Error Handling**: Implement a Global Error Handler and Interceptors for HTTP calls.
- **Asynchronous Operations**: Handle RxJS subscriptions carefully (use `takeUntil`, `AsyncPipe`, or Signals) to avoid memory leaks.
- **Aesthetics & UI/UX**: The UI must feel premium. Utilize daisyUI's elegant themes, smooth transitions, and ensure it is fully responsive. Never leave placeholders or unstyled native HTML elements.
- **Workflow & Commits**: Ensure components render correctly without errors before proceeding. Add comments for complex UI state management logic.
