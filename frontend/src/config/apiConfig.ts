/**
 * API host follows the page hostname so LAN access (e.g. http://192.168.x.x:3000)
 * talks to the backend on the same machine at http://192.168.x.x:8082.
 * Override with REACT_APP_API_HOST / REACT_APP_API_PORT in .env if needed.
 */
const apiHost =
  process.env.REACT_APP_API_HOST ||
  (typeof window !== 'undefined' ? window.location.hostname : 'localhost');

const apiPort = process.env.REACT_APP_API_PORT || '8082';

export const API_ORIGIN = `http://${apiHost}:${apiPort}`;
export const API_BASE_URL = `${API_ORIGIN}/api`;
