const backendDefaultDomain = 'http://localhost:8080';
const backendDomain = process.env.BACKEND_URL ? process.env.BACKEND_URL : backendDefaultDomain;
export default backendDomain;