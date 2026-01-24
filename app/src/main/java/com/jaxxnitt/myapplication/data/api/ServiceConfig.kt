package com.jaxxnitt.myapplication.data.api

/**
 * =====================================================
 * SERVICE CONFIGURATION
 * =====================================================
 *
 * This file contains all the configuration for external services.
 * Before deploying to production, you must configure at least one
 * service for each category (SMS and Email).
 *
 * For security, consider moving these values to:
 * - BuildConfig fields (via build.gradle)
 * - local.properties (for local development)
 * - Encrypted SharedPreferences (for user-provided keys)
 * - A secure backend that provides these at runtime
 *
 * =====================================================
 * SMS SERVICES (choose one)
 * =====================================================
 *
 * Option 1: Native Android SMS (Free, but requires SEND_SMS permission)
 *   - No configuration needed
 *   - Works offline
 *   - Requires user to grant SMS permission
 *   - May fail on some devices/carriers
 *
 * Option 2: Twilio (Paid, more reliable)
 *   - Sign up at: https://www.twilio.com/
 *   - Get Account SID and Auth Token from Console
 *   - Buy a phone number (~$1/month)
 *   - SMS costs ~$0.0079 per message (US)
 *   - Configure in TwilioConfig object
 *
 * =====================================================
 * EMAIL SERVICES (choose one)
 * =====================================================
 *
 * Option 1: SendGrid (Direct integration)
 *   - Sign up at: https://sendgrid.com/
 *   - Free tier: 100 emails/day
 *   - Get API key from Settings > API Keys
 *   - Verify sender email/domain
 *   - Configure in SendGridConfig object
 *
 * Option 2: Custom Backend API
 *   - Deploy your own backend that handles email sending
 *   - Can use any email provider (SES, Mailgun, SMTP, etc.)
 *   - Configure backend URL in BackendConfig object
 *   - Backend should accept POST to /api/send-alert-email
 *
 * =====================================================
 * EXAMPLE BACKEND IMPLEMENTATIONS
 * =====================================================
 *
 * Node.js/Express example:
 * ```javascript
 * const express = require('express');
 * const sgMail = require('@sendgrid/mail');
 *
 * sgMail.setApiKey(process.env.SENDGRID_API_KEY);
 *
 * app.post('/api/send-alert-email', async (req, res) => {
 *   const { to, subject, body, userName } = req.body;
 *   try {
 *     await sgMail.send({
 *       to,
 *       from: 'alerts@yourdomain.com',
 *       subject,
 *       text: body,
 *     });
 *     res.json({ success: true });
 *   } catch (error) {
 *     res.status(500).json({ success: false, message: error.message });
 *   }
 * });
 * ```
 *
 * Python/Flask example:
 * ```python
 * from flask import Flask, request, jsonify
 * from sendgrid import SendGridAPIClient
 * from sendgrid.helpers.mail import Mail
 *
 * @app.route('/api/send-alert-email', methods=['POST'])
 * def send_alert_email():
 *     data = request.json
 *     message = Mail(
 *         from_email='alerts@yourdomain.com',
 *         to_emails=data['to'],
 *         subject=data['subject'],
 *         plain_text_content=data['body']
 *     )
 *     sg = SendGridAPIClient(os.environ.get('SENDGRID_API_KEY'))
 *     response = sg.send(message)
 *     return jsonify({'success': response.status_code == 202})
 * ```
 *
 * =====================================================
 */
object ServiceConfig {

    /**
     * Check if the app has any notification service configured
     */
    val hasAnyServiceConfigured: Boolean
        get() = hasSmsServiceConfigured || hasEmailServiceConfigured

    /**
     * Check if SMS is configured (either native permission or Twilio)
     */
    val hasSmsServiceConfigured: Boolean
        get() = TwilioConfig.isConfigured // Native SMS doesn't require pre-configuration

    /**
     * Check if Email is configured (either SendGrid or backend)
     */
    val hasEmailServiceConfigured: Boolean
        get() = SendGridConfig.isConfigured || BackendConfig.isConfigured

    /**
     * Get a summary of configured services
     */
    fun getConfigurationSummary(): String {
        val sms = when {
            TwilioConfig.isConfigured -> "Twilio"
            else -> "Native SMS (requires permission)"
        }

        val email = when {
            SendGridConfig.isConfigured -> "SendGrid"
            BackendConfig.isConfigured -> "Custom Backend"
            else -> "Not configured"
        }

        return "SMS: $sms | Email: $email"
    }
}
