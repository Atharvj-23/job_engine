const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");

admin.initializeApp();
const db = admin.firestore();

// Configure Nodemailer transporter (fallback / direct SMTP)
const transporter = nodemailer.createTransport({
  host: process.env.SMTP_HOST || "smtp.gmail.com",
  port: parseInt(process.env.SMTP_PORT || "587"),
  secure: false,
  auth: {
    user: process.env.SMTP_USER || "notifications@jobpulse.app",
    pass: process.env.SMTP_PASS || "sample_smtp_secret"
  }
});

/**
 * Cloud Function: onNewJobPostedTriggerEmail
 * 
 * Listens for new job postings in the Firestore 'job_listings' collection.
 * When a job is posted, finds all job seekers whose saved preferences (industry,
 * minimum salary, remote preference) or active job alerts match the newly posted job,
 * and triggers an email notification to them.
 */
exports.onNewJobPostedTriggerEmail = onDocumentCreated(
  "job_listings/{jobId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) {
      console.log("No data associated with the event");
      return null;
    }

    const job = snapshot.data();
    const jobId = event.params.jobId;

    console.log(`[JobPulse Cloud Function] New job created: ${job.title} at ${job.company} (ID: ${jobId})`);

    const jobTitle = job.title || "Untitled Role";
    const company = job.company || "Leading Employer";
    const industry = job.industry || "General";
    const location = job.location || "Remote";
    const isRemote = job.isRemote === true;
    const salaryText = job.salaryText || (job.salaryMin ? `$${job.salaryMin} / yr` : "Competitive");
    const salaryMin = job.salaryMin || 0;
    const salaryMax = job.salaryMax || 0;
    const jobDescription = job.description || "";

    try {
      // 1. Query all job seekers from user_profiles collection
      const seekersSnapshot = await db
        .collection("user_profiles")
        .where("role", "==", "SEEKER")
        .get();

      // 2. Also query active job_alerts collection
      const alertsSnapshot = await db
        .collection("job_alerts")
        .where("isActive", "==", true)
        .get();

      const matchedSeekerEmails = new Map(); // email -> matchReason

      // A. Match against user saved preferences
      seekersSnapshot.forEach((doc) => {
        const seeker = doc.data();
        if (!seeker.email) return;

        let isMatch = false;
        const reasons = [];

        // Check industry preference
        if (seeker.industry && seeker.industry.toLowerCase() === industry.toLowerCase()) {
          isMatch = true;
          reasons.push(`Target Industry: ${seeker.industry}`);
        }

        // Check minimum salary preference
        if (seeker.minExpectedSalary > 0) {
          const effectiveJobSalary = salaryMax > 0 ? salaryMax : salaryMin;
          if (effectiveJobSalary >= seeker.minExpectedSalary) {
            isMatch = true;
            reasons.push(`Meets your salary expectation ($${seeker.minExpectedSalary}+)`);
          } else {
            // If salary is strictly below seeker's min expectation, don't notify unless industry strongly matches
            if (effectiveJobSalary > 0 && effectiveJobSalary < seeker.minExpectedSalary) {
              isMatch = false;
            }
          }
        }

        // Check remote work preference
        if (seeker.preferredWorkMode === "Remote" && isRemote) {
          reasons.push("Remote position match");
        }

        if (isMatch) {
          matchedSeekerEmails.set(seeker.email, {
            name: seeker.fullName || "Job Seeker",
            reasons: reasons.join(" • ")
          });
        }
      });

      // B. Match against explicit job alerts
      alertsSnapshot.forEach((doc) => {
        const alert = doc.data();
        const alertEmail = alert.userEmail;
        if (!alertEmail) return;

        const alertIndustry = alert.industry || "All";
        const alertMinSalary = alert.minSalary || 0;

        const industryMatches = alertIndustry === "All" || alertIndustry.toLowerCase() === industry.toLowerCase();
        const salaryMatches = alertMinSalary === 0 || (salaryMax >= alertMinSalary || salaryMin >= alertMinSalary);

        if (industryMatches && salaryMatches && alert.emailAlerts !== false) {
          const existing = matchedSeekerEmails.get(alertEmail) || {
            name: alert.userName || "Job Seeker",
            reasons: `Alert match for ${alertIndustry}`
          };
          matchedSeekerEmails.set(alertEmail, existing);
        }
      });

      console.log(`Found ${matchedSeekerEmails.size} matching job seekers for job ${jobTitle}`);

      // 3. Dispatch email notifications to all matching seekers
      const dispatchPromises = [];

      for (const [email, seekerInfo] of matchedSeekerEmails.entries()) {
        const emailSubject = `🎯 New Job Match: ${jobTitle} at ${company}`;

        const htmlContent = `
          <!DOCTYPE html>
          <html>
          <head>
            <meta charset="utf-8">
            <style>
              body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
              .card { max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.06); border: 1px solid #e2e8f0; }
              .header { background: linear-gradient(135deg, #1A365D, #0D9488); padding: 28px 24px; color: white; text-align: left; }
              .badge { display: inline-block; background: rgba(255,255,255,0.2); padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; margin-bottom: 8px; }
              .title { font-size: 22px; font-weight: 700; margin: 0 0 6px 0; }
              .subtitle { font-size: 14px; opacity: 0.9; margin: 0; }
              .body-content { padding: 24px; }
              .reason-box { background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; font-size: 13px; color: #166534; }
              .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 20px; }
              .info-item { background: #f8fafc; padding: 12px; border-radius: 8px; border: 1px solid #edf2f7; }
              .info-label { font-size: 11px; text-transform: uppercase; color: #64748b; font-weight: 600; margin-bottom: 4px; }
              .info-val { font-size: 14px; font-weight: 600; color: #0f172a; }
              .desc { font-size: 14px; line-height: 1.6; color: #334155; margin-bottom: 24px; }
              .btn { display: block; text-align: center; background: #0D9488; color: #ffffff !important; text-decoration: none; padding: 14px 24px; border-radius: 10px; font-weight: 600; font-size: 15px; }
              .footer { background: #f8fafc; padding: 16px 24px; text-align: center; font-size: 12px; color: #94a3b8; border-top: 1px solid #e2e8f0; }
            </style>
          </head>
          <body>
            <div class="card">
              <div class="header">
                <div class="badge">MATCHED TO YOUR PREFERENCES</div>
                <h1 class="title">${jobTitle}</h1>
                <p class="subtitle">${company} • ${location} ${isRemote ? "(Remote)" : ""}</p>
              </div>
              <div class="body-content">
                <div class="reason-box">
                  <strong>Why you're seeing this:</strong> ${seekerInfo.reasons}
                </div>
                <div class="info-grid">
                  <div class="info-item">
                    <div class="info-label">Compensation</div>
                    <div class="info-val">${salaryText}</div>
                  </div>
                  <div class="info-item">
                    <div class="info-label">Industry</div>
                    <div class="info-val">${industry}</div>
                  </div>
                  <div class="info-item">
                    <div class="info-label">Work Mode</div>
                    <div class="info-val">${isRemote ? "100% Remote" : "On-site / Hybrid"}</div>
                  </div>
                  <div class="info-item">
                    <div class="info-label">Hiring Company</div>
                    <div class="info-val">${company}</div>
                  </div>
                </div>
                <div class="desc">
                  ${jobDescription.substring(0, 300)}...
                </div>
                <a href="https://jobpulse.app/jobs/${jobId}" class="btn">View Opening & Submit Application</a>
              </div>
              <div class="footer">
                You received this email because you configured preferences on JobPulse.<br/>
                JobPulse Platform • Automated Career Alerts
              </div>
            </div>
          </body>
          </html>
        `;

        // Method A: Write to Firestore 'mail' collection (Trigger Email extension)
        const mailWritePromise = db.collection("mail").add({
          to: email,
          message: {
            subject: emailSubject,
            html: htmlContent,
            text: `New job match: ${jobTitle} at ${company} (${salaryText}). View opening on JobPulse.`
          },
          jobId: jobId,
          createdAt: admin.firestore.FieldValue.serverTimestamp()
        });

        // Method B: Write delivery audit record to 'email_notifications'
        const auditRecordPromise = db.collection("email_notifications").add({
          recipientEmail: email,
          recipientName: seekerInfo.name,
          jobId: jobId,
          jobTitle: jobTitle,
          company: company,
          industry: industry,
          matchReasons: seekerInfo.reasons,
          status: "DELIVERED",
          deliveredAt: admin.firestore.FieldValue.serverTimestamp()
        });

        // Method C: Also push notification to in-app 'notifications' collection
        const inAppNotifPromise = db.collection("notifications").add({
          recipientEmail: email,
          title: `New ${industry} Match: ${jobTitle}`,
          message: `${company} is hiring for ${jobTitle} (${salaryText}). Matches your preferences!`,
          jobId: jobId,
          isRead: false,
          sentAt: admin.firestore.FieldValue.serverTimestamp()
        });

        dispatchPromises.push(mailWritePromise, auditRecordPromise, inAppNotifPromise);
      }

      await Promise.all(dispatchPromises);
      console.log(`Successfully dispatched email notifications to ${matchedSeekerEmails.size} candidates.`);
      return { success: true, count: matchedSeekerEmails.size };

    } catch (error) {
      console.error("Error executing onNewJobPostedTriggerEmail Cloud Function:", error);
      return { success: false, error: error.message };
    }
  }
);
