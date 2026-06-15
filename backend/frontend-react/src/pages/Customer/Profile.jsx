import { useState, useEffect } from 'react';
import { customerAPI } from '../../utils/api';

function Profile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const { data } = await customerAPI.get('/users/profile');
      setProfile(data);
    } catch (error) {
      console.error('Failed to load profile:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page-container">
      <h1>Profile</h1>

      {profile && (
        <div className="card profile-card">
          <div className="profile-field">
            <span className="label">Name:</span>
            <span className="value">{profile.full_name}</span>
          </div>
          <div className="profile-field">
            <span className="label">Phone:</span>
            <span className="value">{profile.phone_number}</span>
          </div>
          <div className="profile-field">
            <span className="label">Email:</span>
            <span className="value">{profile.email || 'Not provided'}</span>
          </div>
          <div className="profile-field">
            <span className="label">Role:</span>
            <span className="value">{profile.role}</span>
          </div>
          <div className="profile-field">
            <span className="label">Status:</span>
            <span className="value">
              <span className={`badge ${profile.is_active ? 'active' : 'inactive'}`}>
                {profile.is_active ? 'Active' : 'Inactive'}
              </span>
            </span>
          </div>
          <div className="profile-field">
            <span className="label">Verified:</span>
            <span className="value">
              <span className={`badge ${profile.is_verified ? 'verified' : 'unverified'}`}>
                {profile.is_verified ? 'Verified' : 'Not Verified'}
              </span>
            </span>
          </div>
          <div className="profile-field">
            <span className="label">KYC Status:</span>
            <span className="value">
              <span className={`badge kyc-${profile.kyc_status}`}>
                {profile.kyc_status}
              </span>
            </span>
          </div>
          {profile.vehicle_number && (
            <div className="profile-field">
              <span className="label">Vehicle:</span>
              <span className="value">{profile.vehicle_number}</span>
            </div>
          )}
          <div className="profile-field">
            <span className="label">Member Since:</span>
            <span className="value">{new Date(profile.created_at).toLocaleDateString()}</span>
          </div>
        </div>
      )}
    </div>
  );
}

export default Profile;

