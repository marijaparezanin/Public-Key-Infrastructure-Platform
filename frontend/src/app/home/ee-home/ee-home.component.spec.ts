import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EeHomeComponent } from './ee-home.component';

describe('EeHomeComponent', () => {
  let component: EeHomeComponent;
  let fixture: ComponentFixture<EeHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EeHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EeHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
