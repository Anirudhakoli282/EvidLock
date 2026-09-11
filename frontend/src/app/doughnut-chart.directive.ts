import { Directive, ElementRef, OnInit, OnDestroy, Input, OnChanges, SimpleChanges } from '@angular/core';
import Chart from 'chart.js/auto';

@Directive({
  selector: '[appDoughnutChart]'
})
export class DoughnutChartDirective implements OnInit, OnDestroy, OnChanges {
  private chart: any;

  @Input() labels: string[] = ['Verified Intact', 'Integrity Compromised', 'Legal Hold Locked'];
  @Input() chartData: number[] = [0, 0, 0];

  constructor(private el: ElementRef) {}

  ngOnInit(): void {
    this.createDoughnutChart();
    this.updateChartSize();
    window.addEventListener('resize', this.updateChartSize.bind(this));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.chart) {
      this.chart.data.labels = this.labels;
      this.chart.data.datasets[0].data = this.chartData;
      this.chart.update();
    }
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.updateChartSize.bind(this));
    if (this.chart) {
      this.chart.destroy();
    }
  }

  private createDoughnutChart(): void {
    const canvas = this.el.nativeElement;
    const ctx = canvas.getContext('2d');

    this.chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: this.labels,
        datasets: [{
          data: this.chartData,
          backgroundColor: [
            '#198754', // Green: Verified Intact
            '#dc3545', // Red: Compromised
            '#0dcaf0'  // Cyan: Legal Hold
          ],
          hoverOffset: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              boxWidth: 12,
              font: {
                size: 11
              }
            }
          }
        }
      }
    });
  }

  private updateChartSize(): void {
    const canvas = this.el.nativeElement;
    const parent = canvas.parentElement;
    if (parent) {
      canvas.width = parent.offsetWidth;
      canvas.height = parent.offsetHeight;
      if (this.chart) {
        this.chart.resize();
      }
    }
  }
}
