# Environment Variables

Create a `.env` file in the `frontend-react` directory with:

```env
# Customer API Base URL (default: http://localhost:8000)
VITE_CUSTOMER_API=http://localhost:8000

# Pump API Base URL (default: http://localhost:8001)
VITE_PUMP_API=http://localhost:8001
```

## For Same Network Access (No ngrok!)

Keep localhost URLs in `.env`, and run:

```bash
npm run dev -- --host
```

Then access from any device on your WiFi using your computer's IP:
`http://192.168.1.XXX:5173`

## For Production or ngrok:

```env
VITE_CUSTOMER_API=https://your-ngrok-url.ngrok-free.app
VITE_PUMP_API=https://your-pump-ngrok-url.ngrok-free.app
```

