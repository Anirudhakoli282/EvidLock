import { environment } from '../environments/environment';

export class Settings {
  /**
   * Returns the backend API base URL.
   * - Development: http://localhost:8081/ (from environment.ts)
   * - Production:  your Render.com URL (from environment.prod.ts, set via RENDER_API_URL build var)
   */
  static get API_ENDPOINT(): string {
    return environment.apiUrl;
  }
}